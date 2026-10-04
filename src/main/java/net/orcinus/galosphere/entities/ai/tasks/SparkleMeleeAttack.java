package net.orcinus.galosphere.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.orcinus.galosphere.entities.Sparkle;

public class SparkleMeleeAttack extends Behavior<Sparkle> {
    private static final double REACH_SQ = 2.5 * 2.5;
    private static final int COOLDOWN_TICKS = 20;

    public SparkleMeleeAttack() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Sparkle sparkle) {
        LivingEntity target = this.target(sparkle);
        return !sparkle.hasCrystal() && target != null && target.isAlive() && target.level() == sparkle.level();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Sparkle sparkle, long gameTime) {
        return this.checkExtraStartConditions(level, sparkle);
    }

    @Override
    protected void tick(ServerLevel level, Sparkle sparkle, long gameTime) {
        LivingEntity target = this.target(sparkle);
        if (target == null) {
            return;
        }
        BehaviorUtils.lookAtEntity(sparkle, target);
        if (sparkle.distanceToSqr(target) <= REACH_SQ) {
            sparkle.doHurtTarget(target);
            sparkle.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, COOLDOWN_TICKS);
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(sparkle, target, 1.0F, 1);
        }
    }

    private LivingEntity target(Sparkle sparkle) {
        return sparkle.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }
}
