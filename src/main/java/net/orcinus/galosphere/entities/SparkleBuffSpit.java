package net.orcinus.galosphere.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.orcinus.galosphere.init.GEntityTypes;
import net.orcinus.galosphere.init.GParticleTypes;

public class SparkleBuffSpit extends AbstractArrow {
    private static final int MAX_LIFE_TICKS = 100;
    private static final double HOMING_STRENGTH = 0.15;
    private static final int BUFF_DURATION_TICKS = 200;
    private int ticksAlive;
    private Player buffTarget;

    public SparkleBuffSpit(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public SparkleBuffSpit(Sparkle shooter, Player buffTarget, Level level) {
        super(GEntityTypes.SPARKLE_BUFF_SPIT, level);
        this.setOwner(shooter);
        this.buffTarget = buffTarget;
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            this.applyHoming();
        }
        super.tick();
        if (!this.level().isClientSide() && this.ticksAlive++ > MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    private void applyHoming() {
        if (this.buffTarget == null || !this.buffTarget.isAlive() || this.buffTarget.level() != this.level()) {
            return;
        }
        Vec3 velocity = this.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 1.0E-5) {
            return;
        }
        Vec3 aim = this.buffTarget.position().add(0.0, this.buffTarget.getBbHeight() * 0.5, 0.0);
        Vec3 toTarget = aim.subtract(this.position());
        if (toTarget.lengthSqr() < 1.0E-5) {
            return;
        }
        Vec3 desiredDir = toTarget.normalize();
        Vec3 currentDir = velocity.scale(1.0 / speed);
        Vec3 newDir = currentDir.scale(1.0 - HOMING_STRENGTH).add(desiredDir.scale(HOMING_STRENGTH)).normalize();
        this.setDeltaMovement(newDir.scale(speed));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // This dart only ever does something on the one entity it was fired at — the
        // owner. Anything else it happens to clip (an enemy stepping into the path)
        // just absorbs it harmlessly; no damage is ever dealt by this projectile.
        if (result.getEntity() == this.buffTarget && this.buffTarget != null) {
            this.buffTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_DURATION_TICKS, 2, false, true));
            this.spawnHitParticles();
        }
        this.discard();
    }

    private void spawnHitParticles() {
        if (this.buffTarget == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(GParticleTypes.LUMIERE_RAIN, this.buffTarget.getX(), this.buffTarget.getY() + this.buffTarget.getBbHeight() * 0.5, this.buffTarget.getZ(), 16, 0.3, 0.3, 0.3, 0.08);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.LLAMA_SPIT;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        // Must stay non-empty: AbstractArrow.addAdditionalSaveData always serializes this
        // stack, and saving an empty ItemStack throws. Pickup itself is DISALLOWED by
        // default, so this item is never actually shown to or collectible by a player.
        return new ItemStack(Items.STICK);
    }
}
