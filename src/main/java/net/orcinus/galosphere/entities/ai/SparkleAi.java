package net.orcinus.galosphere.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.TryFindLand;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;
import net.orcinus.galosphere.entities.Sparkle;
import net.orcinus.galosphere.entities.ai.tasks.FollowOwner;
import net.orcinus.galosphere.entities.ai.tasks.SitSuppressed;
import net.orcinus.galosphere.entities.ai.tasks.SparkleMeleeAttack;
import net.orcinus.galosphere.entities.ai.tasks.SparkleRangedAttack;
import net.orcinus.galosphere.entities.ai.tasks.SparkleSupportBuff;
import net.orcinus.galosphere.entities.ai.tasks.SuppressedWhile;
import net.orcinus.galosphere.entities.ai.tasks.WalkToPollinatedCluster;
import net.orcinus.galosphere.init.GEntityTypes;
import net.orcinus.galosphere.init.GItemTags;

public class SparkleAi {

    public static Brain<?> makeBrain(Brain<Sparkle> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initSwimActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Sparkle> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new SuppressedWhile(new AnimalPanic<>(1.1F), sparkle -> sparkle.isOrderedToSit() || sparkle.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                StopAttackingIfTargetInvalid.<Sparkle>create(),
                new SitSuppressed(new SparkleMeleeAttack()),
                new SitSuppressed(new SparkleRangedAttack()),
                new SitSuppressed(new SparkleSupportBuff())
        ));
    }

    private static void initIdleActivity(Brain<Sparkle> brain) {
        brain.addActivityWithConditions(Activity.IDLE, ImmutableList.of(
                        Pair.of(0, inCombat(new AnimalMakeLove(GEntityTypes.SPARKLE, 1.0F, 2))),
                        Pair.of(1, new FollowOwner(1.0F, 2)),
                        Pair.of(2, inCombat(new SitSuppressed(new FollowTemptation((entity) -> 1.1F)))),
                        Pair.of(3, inCombat(new SitSuppressed(BabyFollowAdult.create(UniformInt.of(5, 16), 1.1f)))),
                        Pair.of(4, inCombat(new SitSuppressed(new WalkToPollinatedCluster()))),
                        Pair.of(5, inCombat(new SitSuppressed(TryFindLand.create(6, 1.0F)))),
                        Pair.of(6, inCombat(new SitSuppressed(new RunOne<>(
                                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                                ImmutableList.of(
                                        Pair.of(RandomStroll.stroll(1.0F), 1),
                                        Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                                        Pair.of(BehaviorBuilder.triggerIf(Entity::onGround), 2)
                                )))))
                ),
                ImmutableSet.of(Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)));
    }

    private static void initSwimActivity(Brain<Sparkle> brain) {
        brain.addActivityWithConditions(Activity.SWIM, ImmutableList.of(
                        Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                        Pair.of(1, new FollowOwner(1.0F, 2)),
                        Pair.of(2, inCombat(new SitSuppressed(new FollowTemptation((entity) -> 1.1F)))),
                        Pair.of(3, inCombat(new SitSuppressed(new WalkToPollinatedCluster()))),
                        Pair.of(4, inCombat(new SitSuppressed(TryFindLand.create(8, 1.1F)))),
                        Pair.of(5, inCombat(new SitSuppressed(new GateBehavior<>(
                                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                                ImmutableSet.of(),
                                GateBehavior.OrderPolicy.ORDERED,
                                GateBehavior.RunningPolicy.TRY_ALL,
                                ImmutableList.of(
                                        Pair.of(RandomStroll.swim(0.75F), 1),
                                        Pair.of(RandomStroll.stroll(1.0F, true), 1),
                                        Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                                        Pair.of(BehaviorBuilder.triggerIf(Entity::isInWaterOrBubble), 5)
                                )))))),
                ImmutableSet.of(Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_PRESENT)));
    }

    private static SuppressedWhile inCombat(BehaviorControl<? super Sparkle> behavior) {
        return new SuppressedWhile(behavior, sparkle -> sparkle.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    public static void updateActivity(Sparkle entity) {
        entity.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.SWIM, Activity.IDLE));
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(GItemTags.SPARKLE_ANY_TEMPT_ITEMS);
    }

}