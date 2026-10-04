package net.orcinus.galosphere.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.orcinus.galosphere.entities.Sparkle;
import net.orcinus.galosphere.entities.SparkleBuffSpit;

/**
 * Lumiere support ability: while a hostile is actively targeting the owner and the
 * Sparkle is in range, it fires a buff dart at the owner instead of fighting itself.
 */
public class SparkleSupportBuff extends Behavior<Sparkle> {
    private static final double MAX_RANGE = 12.0;
    private static final double ENEMY_SCAN_RADIUS = 20.0;
    private static final double CLOSE_RANGE = 2.0;
    private static final double FACING_DOT = 0.5;
    private static final int COOLDOWN_TICKS = 100;

    public SparkleSupportBuff() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Sparkle sparkle) {
        if (!sparkle.hasCrystal() || sparkle.isBaby() || !sparkle.isTame() || sparkle.getVariant() != Sparkle.BirthType.LUMIERE) {
            return false;
        }
        LivingEntity owner = sparkle.getOwner();
        return owner instanceof Player player && player.isAlive() && player.level() == sparkle.level()
                && sparkle.distanceTo(player) <= MAX_RANGE && this.isBeingTargeted(sparkle, player);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Sparkle sparkle, long gameTime) {
        return this.checkExtraStartConditions(level, sparkle);
    }

    @Override
    protected void tick(ServerLevel level, Sparkle sparkle, long gameTime) {
        if (!(sparkle.getOwner() instanceof Player player)) {
            return;
        }
        BehaviorUtils.lookAtEntity(sparkle, player);
        boolean onCooldown = sparkle.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_COOLING_DOWN);
        if (!onCooldown && this.hasClearShot(sparkle, player) && this.isFacing(sparkle, player)) {
            this.shoot(sparkle, player);
            sparkle.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, COOLDOWN_TICKS);
        }
    }

    private boolean isBeingTargeted(Sparkle sparkle, Player player) {
        AABB area = player.getBoundingBox().inflate(ENEMY_SCAN_RADIUS);
        return !sparkle.level().getEntitiesOfClass(Mob.class, area, mob -> mob.getTarget() == player).isEmpty();
    }

    private boolean isFacing(Sparkle sparkle, Player player) {
        double dx = player.getX() - sparkle.getX();
        double dz = player.getZ() - sparkle.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < CLOSE_RANGE) {
            return true;
        }
        Vec3 look = sparkle.getViewVector(1.0F);
        double lookLength = Math.sqrt(look.x * look.x + look.z * look.z);
        return lookLength > 1.0E-4 && (look.x * dx + look.z * dz) / (dist * lookLength) >= FACING_DOT;
    }

    private boolean hasClearShot(Sparkle sparkle, Player player) {
        Vec3 origin = this.shotOrigin(sparkle);
        Vec3 aim = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        BlockHitResult hit = sparkle.level().clip(new ClipContext(origin, aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sparkle));
        return hit.getType() == HitResult.Type.MISS;
    }

    private Vec3 shotOrigin(Sparkle sparkle) {
        Vec3 look = sparkle.getViewVector(1.0F);
        double forwardOffset = sparkle.getBbWidth() / 2.0 + 0.9;
        return new Vec3(sparkle.getX() + look.x * forwardOffset, sparkle.getEyeY() - 0.1, sparkle.getZ() + look.z * forwardOffset);
    }

    private void shoot(Sparkle sparkle, Player player) {
        SparkleBuffSpit spit = new SparkleBuffSpit(sparkle, player, sparkle.level());
        Vec3 origin = this.shotOrigin(sparkle);
        spit.setPos(origin.x, origin.y, origin.z);
        Vec3 aim = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        Vec3 delta = aim.subtract(origin);
        spit.shoot(delta.x, delta.y, delta.z, 1.6F, 2.0F);
        sparkle.level().addFreshEntity(spit);
        sparkle.level().playSound(null, sparkle, Block.byItem(sparkle.getVariant().getSilktouchItem()).defaultBlockState().getSoundType().getBreakSound(), SoundSource.NEUTRAL, 1.0F, 1.0F + (sparkle.getRandom().nextFloat() - sparkle.getRandom().nextFloat()) * 0.2F);
    }
}
