package net.orcinus.galosphere.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.orcinus.galosphere.entities.Sparkle;
import net.orcinus.galosphere.entities.SparkleSpit;

public class SparkleRangedAttack extends Behavior<Sparkle> {
    private static final double MAX_RANGE = 10.0;
    private static final int COOLDOWN_TICKS = 20;
    private static final double FACING_DOT_THRESHOLD = 0.8; // ~37 degrees off-center allowed
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
        boolean canSee = sparkle.getSensing().hasLineOfSight(target);
        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }
        boolean onCooldown = sparkle.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_COOLING_DOWN);
        double distSq = sparkle.distanceToSqr(target);
        boolean facingTarget = this.isFacing(sparkle, target);
        if (!onCooldown && distSq <= MAX_RANGE * MAX_RANGE && canSee && facingTarget && this.seeTime >= 5) {
            this.shoot(sparkle, target);
            sparkle.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, COOLDOWN_TICKS);
            this.seeTime = 0;
        } else if (distSq > MAX_RANGE * MAX_RANGE * 0.5) {
            BehaviorUtils.setWalkAndLookTargetMemories(sparkle, target, 1.0F, (int) Math.sqrt(MAX_RANGE));
        }
    }

    private boolean isFacing(Sparkle sparkle, LivingEntity target) {
        Vec3 toTarget = new Vec3(target.getX() - sparkle.getX(), target.getY(0.5) - sparkle.getEyeY(), target.getZ() - sparkle.getZ()).normalize();
        Vec3 look = sparkle.getViewVector(1.0F);
        return look.dot(toTarget) >= FACING_DOT_THRESHOLD;
    }

    private void shoot(Sparkle sparkle, LivingEntity target) {
        SparkleSpit spit = new SparkleSpit(sparkle, sparkle.level());
        Vec3 look = sparkle.getViewVector(1.0F);
        double forwardOffset = sparkle.getBbWidth() / 2.0 + 0.4;
        double originX = sparkle.getX() + look.x * forwardOffset;
        double originY = sparkle.getEyeY() - 0.1;
        double originZ = sparkle.getZ() + look.z * forwardOffset;
        spit.setPos(originX, originY, originZ);
        double dx = target.getX() - originX;
        double dy = target.getY(0.5) - originY;
        double dz = target.getZ() - originZ;
        spit.shoot(dx, dy, dz, 1.6F, 2.0F);
        sparkle.level().addFreshEntity(spit);
        sparkle.level().playSound(null, sparkle, SoundEvents.LLAMA_SPIT, SoundSource.NEUTRAL, 1.0F, 1.0F + (sparkle.getRandom().nextFloat() - sparkle.getRandom().nextFloat()) * 0.2F);
    }
}
