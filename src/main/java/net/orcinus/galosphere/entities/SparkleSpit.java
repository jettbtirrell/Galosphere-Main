package net.orcinus.galosphere.entities;

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
import net.orcinus.galosphere.init.GEntityTypes;

public class SparkleSpit extends AbstractArrow {
    private static final int MAX_LIFE_TICKS = 100;
    private Sparkle.BirthType birthType = Sparkle.BirthType.ALLURITE;
    private int ticksAlive;

    public SparkleSpit(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public SparkleSpit(Sparkle shooter, Level level) {
        super(GEntityTypes.SPARKLE_SPIT, level);
        this.setOwner(shooter);
        this.birthType = shooter.getVariant();
        this.setBaseDamage(5.0F);
        this.setNoGravity(true);
    }

    public Sparkle.BirthType getBirthType() {
        return this.birthType;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.ticksAlive++ > MAX_LIFE_TICKS) {
            this.discard();
        }
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
            super.onHitEntity(result);
            if (target instanceof LivingEntity living) {
                if (this.birthType == Sparkle.BirthType.LUMIERE) {
                    living.setRemainingFireTicks(100);
                } else {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, true));
                }
            }
        }
        // Always vanish on contact, whether the hit was applied or skipped — prevents it
        // from lingering with glitched-out velocity when a hit doesn't cleanly resolve
        // (e.g. the target is invulnerable, or this is a friendly-fire pass-through).
        this.discard();
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
