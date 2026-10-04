package net.orcinus.galosphere.entities.ai.sensors;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;
import net.orcinus.galosphere.entities.Sparkle;

import java.util.Comparator;
import java.util.Set;

public class OwnerCombatSensor extends Sensor<Sparkle> {
    private static final double AGGRO_RANGE = 16.0;
    private int lastOwnerHurtByTimestamp;
    private int lastOwnerHurtTimestamp;

    @Override
    protected void doTick(ServerLevel level, Sparkle sparkle) {
        LivingEntity owner = sparkle.getOwner();
        if (owner == null || !sparkle.isTame()) {
            return;
        }
        LivingEntity hurtBy = owner.getLastHurtByMob();
        if (hurtBy != null && owner.getLastHurtByMobTimestamp() != this.lastOwnerHurtByTimestamp) {
            this.lastOwnerHurtByTimestamp = owner.getLastHurtByMobTimestamp();
            if (hurtBy.isAlive() && hurtBy.level() == sparkle.level() && sparkle.canAttack(hurtBy)) {
                sparkle.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, hurtBy);
                return;
            }
        }
        LivingEntity ownerTarget = owner.getLastHurtMob();
        if (ownerTarget != null && owner.getLastHurtMobTimestamp() != this.lastOwnerHurtTimestamp) {
            this.lastOwnerHurtTimestamp = owner.getLastHurtMobTimestamp();
            if (ownerTarget.isAlive() && ownerTarget.level() == sparkle.level() && sparkle.canAttack(ownerTarget)) {
                sparkle.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, ownerTarget);
                return;
            }
        }
        if (!sparkle.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            this.findMonsterAggroingOwner(sparkle, owner);
        }
    }

    private void findMonsterAggroingOwner(Sparkle sparkle, LivingEntity owner) {
        AABB area = sparkle.getBoundingBox().inflate(AGGRO_RANGE);
        sparkle.level().getEntitiesOfClass(Mob.class, area, mob -> mob.getTarget() == owner && sparkle.canAttack(mob))
                .stream()
                .min(Comparator.comparingDouble(sparkle::distanceToSqr))
                .ifPresent(mob -> sparkle.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, mob));
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.ATTACK_TARGET);
    }
}
