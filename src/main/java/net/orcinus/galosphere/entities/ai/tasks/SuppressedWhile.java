package net.orcinus.galosphere.entities.ai.tasks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.orcinus.galosphere.entities.Sparkle;

import java.util.function.Predicate;

public class SuppressedWhile implements BehaviorControl<Sparkle> {
    private final BehaviorControl<? super Sparkle> wrapped;
    private final Predicate<Sparkle> suppress;

    public SuppressedWhile(BehaviorControl<? super Sparkle> wrapped, Predicate<Sparkle> suppress) {
        this.wrapped = wrapped;
        this.suppress = suppress;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.wrapped.getStatus();
    }

    @Override
    public boolean tryStart(ServerLevel level, Sparkle sparkle, long gameTime) {
        if (this.suppress.test(sparkle)) {
            return false;
        }
        return this.wrapped.tryStart(level, sparkle, gameTime);
    }

    @Override
    public void tickOrStop(ServerLevel level, Sparkle sparkle, long gameTime) {
        this.wrapped.tickOrStop(level, sparkle, gameTime);
    }

    @Override
    public void doStop(ServerLevel level, Sparkle sparkle, long gameTime) {
        this.wrapped.doStop(level, sparkle, gameTime);
    }

    @Override
    public String debugString() {
        return "suppressedWhile(" + this.wrapped.debugString() + ")";
    }
}
