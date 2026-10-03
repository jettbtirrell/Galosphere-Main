package net.orcinus.galosphere.entities.ai.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.orcinus.galosphere.entities.Sparkle;

public class FollowOwner extends Behavior<Sparkle> {
    private final float speedModifier;
    private final int closeEnoughDist;

    public FollowOwner(float speedModifier, int closeEnoughDist) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = speedModifier;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Sparkle sparkle) {
        LivingEntity owner = sparkle.getOwner();
        return sparkle.isTame() && !sparkle.unableToMoveToOwner() && owner != null && owner.isAlive()
                && owner.level() == sparkle.level()
                && sparkle.distanceToSqr(owner) > (double) (this.closeEnoughDist * this.closeEnoughDist);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Sparkle sparkle, long gameTime) {
        return this.checkExtraStartConditions(level, sparkle);
    }

    @Override
    protected void tick(ServerLevel level, Sparkle sparkle, long gameTime) {
        LivingEntity owner = sparkle.getOwner();
        if (owner != null) {
            if (sparkle.shouldTryTeleportToOwner()) {
                sparkle.tryToTeleportToOwner();
            } else {
                BehaviorUtils.setWalkAndLookTargetMemories(sparkle, owner, this.speedModifier, this.closeEnoughDist);
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, Sparkle sparkle, long gameTime) {
        sparkle.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}
