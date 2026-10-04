package net.orcinus.galosphere.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.orcinus.galosphere.crafting.GlintingManager;
import net.orcinus.galosphere.Galosphere;
import net.orcinus.galosphere.entities.ai.SparkleAi;
import net.orcinus.galosphere.entities.navigation.SemiAquaticPathNavigation;
import net.orcinus.galosphere.init.GBlockTags;
import net.orcinus.galosphere.init.GBlocks;
import net.orcinus.galosphere.init.GEntityTypes;
import net.orcinus.galosphere.init.GItemTags;
import net.orcinus.galosphere.init.GItems;
import net.orcinus.galosphere.init.GMemoryModuleTypes;
import net.orcinus.galosphere.init.GSensorTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.IntFunction;

public class Sparkle extends TamableAnimal implements VariantHolder<Sparkle.BirthType>, NeutralMob {
    private static final EntityDataAccessor<Integer> BIRTH_TYPE = SynchedEntityData.defineId(Sparkle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_CRYSTAL = SynchedEntityData.defineId(Sparkle.class, EntityDataSerializers.BOOLEAN);
    private static final UniformInt PERSISTENT_ANGER_TIME = UniformInt.of(400, 800);
    private static final int TELEPORT_SCAN_DEPTH = 64;
    private static final int TELEPORT_COOLDOWN_TICKS = 40;
    private int lastTeleportTick = -TELEPORT_COOLDOWN_TICKS;
    private static final byte CRYSTAL_MAXED_EVENT = 20;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Sparkle>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, GSensorTypes.SPARKLE_TEMPTATIONS, GSensorTypes.NEAREST_POLLINATED_CLUSTER, GSensorTypes.OWNER_COMBAT_SENSOR, SensorType.IS_IN_WATER);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.BREED_TARGET, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.IS_IN_WATER, MemoryModuleType.IS_PANICKING, GMemoryModuleTypes.NEAREST_POLLINATED_CLUSTER, GMemoryModuleTypes.POLLINATED_COOLDOWN, GMemoryModuleTypes.SIT_ORIGIN, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);
    private static final UniformInt REGROWTH_TICKS = UniformInt.of(6000, 12000);
    private final Map<Block, Block> clustersToGlinted = GlintingManager.getGlintingTable();
    private int growthTicks;
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;

    public Sparkle(EntityType<? extends Sparkle> type, Level world) {
        super(type, world);
        this.setPathfindingMalus(PathType.WATER, 4.0F);
        this.setPathfindingMalus(PathType.TRAPDOOR, -1.0F);
        this.moveControl = new AmphibiousMoveControl(this);
    }

    @Override
    public float maxUpStep() {
        return 1.0F;
    }

    public Map<Block, Block> getClustersToGlinted() {
        return this.clustersToGlinted;
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return 0.0F;
    }

    @Override
    protected Brain.Provider<Sparkle> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return SparkleAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<Sparkle> getBrain() {
        return (Brain<Sparkle>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.updatePersistentAnger((ServerLevel) this.level(), true);
        this.syncAngerTarget();
        this.level().getProfiler().push("sparkleBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("sparkleActivityUpdate");
        SparkleAi.updateActivity(this);
        this.level().getProfiler().pop();
        if (this.tickCount % 20 == 0) {
            this.logSpeedDebug();
        }
        super.customServerAiStep();
    }

    public static void recallOwnedSparkles(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!player.getRespawnDimension().equals(level.dimension())) {
            return;
        }
        BlockPos respawn = player.getRespawnPosition() != null ? player.getRespawnPosition() : level.getSharedSpawnPos();
        level.getChunk(respawn.getX() >> 4, respawn.getZ() >> 4);
        level.getEntities(EntityTypeTest.forClass(Sparkle.class), sparkle -> sparkle.isTame() && !sparkle.isOrderedToSit() && player.getUUID().equals(sparkle.getOwnerUUID()))
                .forEach(sparkle -> sparkle.teleportToGroundFrom(respawn));
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int remainingPersistentAngerTime) {
        this.remainingPersistentAngerTime = remainingPersistentAngerTime;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID persistentAngerTarget) {
        this.persistentAngerTarget = persistentAngerTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    private void syncAngerTarget() {
        if (this.isTame()) {
            return;
        }
        if (!this.isAngry()) {
            this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return;
        }
        UUID targetUuid = this.getPersistentAngerTarget();
        if (targetUuid != null && this.level() instanceof ServerLevel serverLevel) {
            Entity target = serverLevel.getEntity(targetUuid);
            if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingTarget);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide()) {
            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity livingAttacker && this.canAttack(livingAttacker)) {
                if (this.isTame()) {
                    // Tamed Sparkles retaliate against whoever directly hit them, even if
                    // the owner was never touched — otherwise a lost/timed-out ATTACK_TARGET
                    // leaves them defenseless (FollowOwner takes back over and they just
                    // run home while getting hit).
                    this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingAttacker);
                } else {
                    this.setPersistentAngerTarget(livingAttacker.getUUID());
                    this.startPersistentAngerTimer();
                    this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingAttacker);
                    this.alertNearbyWildSparkles(livingAttacker);
                }
            }
        }
        return hurt;
    }

    private void alertNearbyWildSparkles(LivingEntity attacker) {
        AABB aabb = this.getBoundingBox().inflate(16.0, 10.0, 16.0);
        for (Sparkle sparkle : this.level().getEntitiesOfClass(Sparkle.class, aabb, sparkle -> sparkle != this && !sparkle.isTame())) {
            sparkle.setPersistentAngerTarget(attacker.getUUID());
            sparkle.startPersistentAngerTimer();
            sparkle.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return !this.isTame() && super.removeWhenFarAway(distanceSq);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.isTame();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    private static class AmphibiousMoveControl extends MoveControl {
        private final SmoothSwimmingMoveControl swimming;

        AmphibiousMoveControl(Mob mob) {
            super(mob);
            this.swimming = new SmoothSwimmingMoveControl(mob, 85, 10, 1.1F, 1.0F, true);
        }

        @Override
        public void setWantedPosition(double x, double y, double z, double speed) {
            super.setWantedPosition(x, y, z, speed);
            this.swimming.setWantedPosition(x, y, z, speed);
        }

        @Override
        public void strafe(float forward, float side) {
            super.strafe(forward, side);
            this.swimming.strafe(forward, side);
        }

        @Override
        public void tick() {
            if (this.mob.isInWater()) {
                this.swimming.tick();
            } else {
                super.tick();
            }
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        } else {
            super.travel(vec3);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    private static final double BASE_MOVEMENT_SPEED = 0.3;
    private static final double LUMIERE_GROWN_MOVEMENT_SPEED = 0.35;
    private static final double ALLURITE_GROWN_MOVEMENT_SPEED = 0.25;

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED).add(Attributes.MAX_HEALTH, 15.0).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0);
            this.setHealth(60.0F);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(15.0);
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        return effectInstance.getEffect() != MobEffects.MOVEMENT_SLOWDOWN && super.canBeAffected(effectInstance);
    }

    @Override
    public int getMaxHeadXRot() {
        return 30;
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BIRTH_TYPE, 0);
        builder.define(HAS_CRYSTAL, true);
    }

    public static boolean checkSparkleSpawnRules(EntityType<? extends LivingEntity> sparkle, LevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return world.getBlockState(pos.below()).is(GBlockTags.SPARKLES_SPAWNABLE_ON);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(this.getRandomType());
        this.setHasCrystal(true);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(BirthType.byId(tag.getInt("BirthType")));
        this.setHasCrystal(tag.getBoolean("HasCrystal"));
        this.setGrowthTicks(tag.getInt("GrowthTicks"));
        this.readPersistentAngerSaveData((ServerLevel) this.level(), tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BirthType", this.getVariant().getId());
        tag.putBoolean("HasCrystal", this.hasCrystal());
        tag.putInt("GrowthTicks", this.getGrowthTicks());
        this.addPersistentAngerSaveData(tag);
    }

    public void setGrowthTicks(int growthTicks) {
        this.growthTicks = growthTicks;
    }

    public int getGrowthTicks() {
        return this.growthTicks;
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new SemiAquaticPathNavigation(this, world);
    }

    @Override
    public void setVariant(BirthType birthType) {
        this.entityData.set(BIRTH_TYPE, birthType.getId());
        this.updateMovementSpeed();
    }

    private void updateMovementSpeed() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        double value = BASE_MOVEMENT_SPEED;
        if (this.hasCrystal()) {
            value = this.getVariant() == BirthType.LUMIERE ? LUMIERE_GROWN_MOVEMENT_SPEED : ALLURITE_GROWN_MOVEMENT_SPEED;
        }
        speed.setBaseValue(value);
    }

    @Override
    public BirthType getVariant() {
        return BirthType.byId(this.entityData.get(BIRTH_TYPE));
    }

    @Override
    public void tryToTeleportToOwner() {
        if (this.tickCount - this.lastTeleportTick < TELEPORT_COOLDOWN_TICKS) {
            return;
        }
        LivingEntity owner = this.getOwner();
        if (owner != null && this.teleportToGroundFrom(owner.blockPosition())) {
            this.lastTeleportTick = this.tickCount;
        }
    }

    public boolean teleportToGroundFrom(BlockPos from) {
        BlockPos ground = this.findGroundBelow(from);
        if (ground == null) {
            return false;
        }
        this.moveTo(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5);
        return true;
    }

    private BlockPos findGroundBelow(BlockPos from) {
        int lowest = Math.max(this.level().getMinBuildHeight(), from.getY() - TELEPORT_SCAN_DEPTH);
        for (BlockPos feet = from; feet.getY() >= lowest; feet = feet.below()) {
            BlockPos support = feet.below();
            boolean freeSpace = this.level().getBlockState(feet).getCollisionShape(this.level(), feet).isEmpty() && this.level().getFluidState(feet).isEmpty();
            boolean solidSupport = this.level().getBlockState(support).isFaceSturdy(this.level(), support, Direction.UP);
            if (freeSpace && solidSupport) {
                return feet;
            }
        }
        return null;
    }

    private void logSpeedDebug() {
        var vel = this.getDeltaMovement();
        float moveDirYaw = (float) (Mth.atan2(vel.z, vel.x) * (180.0 / Math.PI)) - 90.0F;
        float yawErr = Mth.wrapDegrees(moveDirYaw - this.getYRot());
        var owner = this.getOwner();
        var nav = this.getNavigation();
        var path = nav.getPath();
        var walk = this.getBrain().getMemory(MemoryModuleType.WALK_TARGET).map(t -> t.getTarget().toString()).orElse("none");
        var attack = this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(e -> e.getName().getString()).orElse("none");
        var activity = this.getBrain().getActiveNonCoreActivity().map(Object::toString).orElse("none");
        var modifiers = this.getAttribute(Attributes.MOVEMENT_SPEED).getModifiers().stream().map(m -> m.id() + "=" + m.amount()).toList();
        var effects = this.getActiveEffects().stream().map(e -> e.getEffect().getRegisteredName() + "x" + (e.getAmplifier() + 1) + "/" + e.getDuration()).toList();
        Galosphere.LOGGER.info("[SparkleSpeed] id={} pos=({}, {}, {}) vel=({}, {}, {}) horizBps={} attrBase={} attrValue={} attrModifiers={} effects={} onGround={} inWater={} swimming={} inPowderSnow={} inBlock={} blockBelow={} crystal={} baby={} sitting={} tamed={} ownerDist={} walk={} attack={} activity={} navDone={} navPathNodes={} navPathIndex={} navStuck={} tickCount={} fallDist={} yRot={} yBodyRot={} yHeadRot={} moveDirYaw={} yawErr={} moveCtrl={} moveHasWanted={} moveSpeedMod={} getSpeed={} lookTarget={} coolingDown={}",
                this.getId(),
                String.format("%.2f", this.getX()), String.format("%.2f", this.getY()), String.format("%.2f", this.getZ()),
                String.format("%.3f", vel.x), String.format("%.3f", vel.y), String.format("%.3f", vel.z),
                String.format("%.3f", Math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20.0),
                String.format("%.3f", this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)),
                String.format("%.3f", this.getAttributeValue(Attributes.MOVEMENT_SPEED)),
                modifiers, effects,
                this.onGround(), this.isInWater(), this.isSwimming(), this.isInPowderSnow,
                this.level().getBlockState(this.blockPosition()).getBlock().builtInRegistryHolder().key().location(),
                this.level().getBlockState(this.blockPosition().below()).getBlock().builtInRegistryHolder().key().location(),
                this.hasCrystal(), this.isBaby(), this.isOrderedToSit(), this.isTame(),
                owner == null ? "none" : String.format("%.2f", this.distanceTo(owner)),
                walk, attack, activity,
                nav.isDone(), path == null ? -1 : path.getNodeCount(), path == null ? -1 : path.getNextNodeIndex(), nav.isStuck(),
                this.tickCount, String.format("%.2f", this.fallDistance),
                String.format("%.1f", this.getYRot()), String.format("%.1f", this.yBodyRot), String.format("%.1f", this.yHeadRot),
                String.format("%.1f", moveDirYaw), String.format("%.1f", yawErr),
                this.getMoveControl().getClass().getSimpleName(), this.getMoveControl().hasWanted(),
                String.format("%.3f", this.getMoveControl().getSpeedModifier()), String.format("%.3f", this.getSpeed()),
                this.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).isPresent(),
                this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_COOLING_DOWN));
    }

    @Override
    public boolean isInSittingPose() {
        return this.isTame() && super.isInSittingPose();
    }

    @Override
    public boolean isOrderedToSit() {
        return this.isTame() && super.isOrderedToSit();
    }

    public boolean hasCrystal() {
        return this.entityData.get(HAS_CRYSTAL);
    }

    public void setHasCrystal(boolean hasCrystal) {
        this.entityData.set(HAS_CRYSTAL, hasCrystal);
        this.updateMovementSpeed();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(GItemTags.SPARKLE_BREED_ITEMS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob mob) {
        Sparkle sparkleEntity = GEntityTypes.SPARKLE.create(world);
        if (sparkleEntity != null) {
            sparkleEntity.setVariant(sparkleEntity.getRandomType());
            sparkleEntity.setHasCrystal(false);
            sparkleEntity.setGrowthTicks(REGROWTH_TICKS.sample(sparkleEntity.getRandom()));
        }
        return sparkleEntity;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            if (this.getGrowthTicks() > 0) {
                this.setGrowthTicks(this.getGrowthTicks() - 1);
            }
            if (this.getGrowthTicks() == 0 && !this.hasCrystal()) {
                this.setHasCrystal(true);
            }
        }
    }

    public BirthType getRandomType() {
        return random.nextBoolean() ? BirthType.ALLURITE : BirthType.LUMIERE;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() && this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.setInSittingPose(this.isOrderedToSit());
                if (this.isOrderedToSit()) {
                    this.getBrain().setMemory(GMemoryModuleTypes.SIT_ORIGIN, this.blockPosition());
                    this.setTarget(null);
                    this.getNavigation().stop();
                } else {
                    this.getBrain().eraseMemory(GMemoryModuleTypes.SIT_ORIGIN);
                }
            }
            return InteractionResult.SUCCESS;
        }
        else if (this.hasCrystal() && stack.getItem() instanceof PickaxeItem && !this.isBaby()) {
            this.extractShard(stack);
            stack.hurtAndBreak(1, player, Sparkle.getSlotForHand(hand));
            this.gameEvent(GameEvent.SHEAR, player);
            return InteractionResult.SUCCESS;
        }
        else if (stack.is(GItemTags.SPARKLE_TEMPT_ITEMS)) {
            boolean maxed = this.isTame() && this.hasCrystal() && this.getHealth() >= this.getMaxHealth();
            if (maxed) {
                return InteractionResult.PASS;
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (!this.hasCrystal()) {
                this.setGrowthTicks(Math.max(0, this.getGrowthTicks() - Mth.nextInt(random, 20, 40)));
            }
            this.heal(2.0F);
            if (!this.isTame()) {
                if (this.random.nextInt(3) == 0) {
                    super.tame(player);
                    this.setPersistenceRequired();
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            if (this.isTame() && this.hasCrystal() && this.getHealth() >= this.getMaxHealth()) {
                this.level().broadcastEntityEvent(this, CRYSTAL_MAXED_EVENT);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == CRYSTAL_MAXED_EVENT) {
            for (int i = 0; i < 7; i++) {
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            }
        } else {
            super.handleEntityEvent(b);
        }
    }

    public void extractShard(ItemStack stack) {
        this.spawnShard(stack);
        this.playSound(SoundEvents.CALCITE_HIT, 1.0F, 1.0F);
        this.setHasCrystal(false);
        this.setGrowthTicks(REGROWTH_TICKS.sample(this.getRandom()));
    }

    private void spawnShard(ItemStack stack) {
        HolderLookup.RegistryLookup<Enchantment> lookup = this.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Item item = EnchantmentHelper.getItemEnchantmentLevel(lookup.getOrThrow(Enchantments.SILK_TOUCH), stack) > 0 ? this.getVariant().getSilktouchItem() : this.getVariant().getItem();
        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(lookup.getOrThrow(Enchantments.FORTUNE), stack);
        int rolls = fortuneLevel > 0 ? Mth.nextInt(random, 0, 2) * fortuneLevel : 1;

        for (int i = 0; i < rolls; i++) {
            this.spawnAtLocation(item);
        }
    }

    public enum BirthType implements StringRepresentable {
        ALLURITE(0, "allurite", GItems.ALLURITE_SHARD, GBlocks.ALLURITE_CLUSTER.asItem()),
        LUMIERE(1, "lumiere", GItems.LUMIERE_SHARD, GBlocks.LUMIERE_CLUSTER.asItem());

        public static final IntFunction<BirthType> BY_ID = ByIdMap.continuous(BirthType::getId, BirthType.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        private final int id;
        private final String name;
        private final Item item;
        private final Item silktouchItem;

        BirthType(int id, String name, Item item, Item silktouchItem) {
            this.id = id;
            this.name = name;
            this.item = item;
            this.silktouchItem = silktouchItem;
        }

        public int getId() {
            return this.id;
        }

        public static BirthType byId(int i) {
            return BY_ID.apply(i);
        }

        public String getName() {
            return this.name;
        }

        public Item getItem() {
            return this.item;
        }

        public Item getSilktouchItem() {
            return this.silktouchItem;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

}