package net.orcinus.galosphere.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.orcinus.galosphere.entities.Sparkle;
import net.orcinus.galosphere.entities.SparkleSpit;

public class SparkleRangedAttack extends Behavior<Sparkle> {
    private static final double MAX_RANGE = 10.0;
    private static final double MIN_RANGE = 4.0;
    private static final double APPROACH_RANGE = 7.0;
    private static final double CLOSE_RANGE = 2.0;
    private static final double FACING_DOT = 0.5;
    private static final int COOLDOWN_TICKS = 20;
    private int seeTime;

    public SparkleRangedAttack() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Sparkle sparkle) {
        LivingEntity target = sparkle.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return sparkle.hasCrystal() && !sparkle.isBaby() && target != null && target.isAlive() && target.level() == sparkle.level();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Sparkle sparkle, long gameTime) {
        return this.checkExtraStartConditions(level, sparkle);
    }

    @Override
    protected void start(ServerLevel level, Sparkle sparkle, long gameTime) {
        this.seeTime = 0;
    }

    @Override
    protected void tick(ServerLevel level, Sparkle sparkle, long gameTime) {
        LivingEntity target = sparkle.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) {
            return;
        }
        BehaviorUtils.lookAtEntity(sparkle, target);
        boolean canSee = this.hasClearShot(sparkle, target);
        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }
        boolean onCooldown = sparkle.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_COOLING_DOWN);
        double distSq = sparkle.distanceToSqr(target);
        if (!onCooldown && distSq <= MAX_RANGE * MAX_RANGE && canSee && this.isFacing(sparkle, target) && this.seeTime >= 5) {
            this.shoot(sparkle, target);
            sparkle.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, COOLDOWN_TICKS);
            this.seeTime = 0;
        }
        if (distSq < MIN_RANGE * MIN_RANGE) {
            this.backAway(sparkle);
        } else if (distSq > APPROACH_RANGE * APPROACH_RANGE) {
            BehaviorUtils.setWalkAndLookTargetMemories(sparkle, target, 1.0F, (int) APPROACH_RANGE);
        }
    }

    private boolean isFacing(Sparkle sparkle, LivingEntity target) {
        double dx = target.getX() - sparkle.getX();
        double dz = target.getZ() - sparkle.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < CLOSE_RANGE) {
            return true;
        }
        Vec3 look = sparkle.getViewVector(1.0F);
        double lookLength = Math.sqrt(look.x * look.x + look.z * look.z);
        return lookLength > 1.0E-4 && (look.x * dx + look.z * dz) / (dist * lookLength) >= FACING_DOT;
    }

    private void backAway(Sparkle sparkle) {
        sparkle.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        sparkle.getNavigation().stop();
        sparkle.getMoveControl().strafe(-1.0F, 0.0F);
    }

    private boolean hasClearShot(Sparkle sparkle, LivingEntity target) {
        Vec3 origin = this.shotOrigin(sparkle);
        Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        BlockHitResult hit = sparkle.level().clip(new ClipContext(origin, aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sparkle));
        return hit.getType() == HitResult.Type.MISS;
    }

    private Vec3 shotOrigin(Sparkle sparkle) {
        Vec3 look = sparkle.getViewVector(1.0F);
        double forwardOffset = sparkle.getBbWidth() / 2.0 + 0.4;
        return new Vec3(sparkle.getX() + look.x * forwardOffset, sparkle.getEyeY() - 0.1, sparkle.getZ() + look.z * forwardOffset);
    }

    private void shoot(Sparkle sparkle, LivingEntity target) {
        SparkleSpit spit = new SparkleSpit(sparkle, sparkle.level());
        Vec3 origin = this.shotOrigin(sparkle);
        spit.setPos(origin.x, origin.y, origin.z);
        Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 delta = aim.subtract(origin);
        spit.shoot(delta.x, delta.y, delta.z, 1.6F, 2.0F);
        sparkle.level().addFreshEntity(spit);
        sparkle.level().playSound(null, sparkle, Block.byItem(sparkle.getVariant().getSilktouchItem()).defaultBlockState().getSoundType().getBreakSound(), SoundSource.NEUTRAL, 1.0F, 1.0F + (sparkle.getRandom().nextFloat() - sparkle.getRandom().nextFloat()) * 0.2F);
    }
}
