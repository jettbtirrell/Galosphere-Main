package net.orcinus.galosphere.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.orcinus.galosphere.init.GEntityTypes;
import net.orcinus.galosphere.init.GParticleTypes;

public class SparkleSpit extends AbstractArrow {
    private static final int MAX_LIFE_TICKS = 100;
    private static final double HOMING_STRENGTH = 0.15;
    private static final int SLOW_DURATION_TICKS = 200;
    private int ticksAlive;
    private LivingEntity target;

    public SparkleSpit(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public SparkleSpit(Sparkle shooter, Level level) {
        super(GEntityTypes.SPARKLE_SPIT, level);
        this.setOwner(shooter);
        this.setBaseDamage(4.0F);
        this.setNoGravity(true);
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
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
        if (this.target == null || !this.target.isAlive() || this.target.level() != this.level()) {
            return;
        }
        Vec3 velocity = this.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 1.0E-5) {
            return;
        }
        Vec3 aim = this.target.position().add(0.0, this.target.getBbHeight() * 0.5, 0.0);
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
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        LivingEntity shooterOwner = owner instanceof Sparkle sparkle ? sparkle.getOwner() : null;
        boolean skip = target == owner
                || (owner != null && owner.isAlliedTo(target))
                || target == shooterOwner
                || (shooterOwner != null && target instanceof OwnableEntity ownable && shooterOwner.getUUID().equals(ownable.getOwnerUUID()));
        if (!skip) {
            float healthBefore = target instanceof LivingEntity living ? living.getHealth() : 0.0F;
            // AbstractArrow scales its hit damage by this projectile's current flight
            // speed (damage = ceil(velocity.length() * baseDamage)), so distance/travel
            // time would otherwise change how much this hits for. Normalize velocity to
            // length 1 right before the vanilla damage calc runs so it multiplies by
            // exactly 1 — damage becomes just baseDamage, independent of range.
            Vec3 deltaMovement = this.getDeltaMovement();
            if (deltaMovement.lengthSqr() > 1.0E-7) {
                this.setDeltaMovement(deltaMovement.normalize());
            }
            super.onHitEntity(result);
            if (target instanceof LivingEntity living && living.getHealth() < healthBefore) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_DURATION_TICKS, 2, false, true));
                this.spawnHitParticles(living);
            }
        }
        // Always vanish on contact, whether the hit was applied or skipped — prevents it
        // from lingering with glitched-out velocity when a hit doesn't cleanly resolve
        // (e.g. the target is invulnerable, or this is a friendly-fire pass-through).
        this.discard();
    }

    private void spawnHitParticles(LivingEntity living) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(GParticleTypes.ALLURITE_RAIN, living.getX(), living.getY() + living.getBbHeight() * 0.5, living.getZ(), 16, 0.3, 0.3, 0.3, 0.08);
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
