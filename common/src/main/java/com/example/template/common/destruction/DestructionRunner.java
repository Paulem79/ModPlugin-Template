package com.example.template.common.destruction;

import com.example.template.common.destruction.model.DestroyedBlock;
import com.example.template.common.destruction.model.StoredEntity;
import com.example.template.common.destruction.state.DestructionState;
import com.example.template.common.destruction.state.PreparationState;

import java.util.function.BooleanSupplier;

/** Runs one destruction through its states: preparation, destroying, waiting, restoring. */
public final class DestructionRunner {
    private final BooleanSupplier ticker;

    private DestructionRunner(BooleanSupplier ticker) {
        this.ticker = ticker;
    }

    /** Wraps a destruction that drives itself (e.g. dispatches work to region threads), polled once per tick. */
    public static DestructionRunner of(BooleanSupplier ticker) {
        return new DestructionRunner(ticker);
    }

    public static <W, K, MD extends DestroyedBlock, ME extends StoredEntity> DestructionRunner of(
            DestructionContext<W, K, MD, ME> context) {
        return new DestructionRunner(new Machine<>(context, new PreparationState<>())::tick);
    }

    /** Runs the destruction for the current tick. Returns {@code true} once it has finished. */
    public boolean tick() {
        return ticker.getAsBoolean();
    }

    private static final class Machine<C extends DestructionContext<?, ?, ?, ?>> {
        private final C context;
        private DestructionState<C> currentState;

        private Machine(C context, DestructionState<C> initialState) {
            this.context = context;
            this.currentState = initialState;
        }

        private boolean tick() {
            long startTime = System.nanoTime();

            while (currentState != null) {
                DestructionState<C> nextState = currentState.tick(context, startTime);

                if (nextState == currentState) {
                    // The current state needs more ticks
                    break;
                }

                currentState = nextState;
                if (currentState != null) {
                    currentState.enter(context);
                }
            }

            return currentState == null;
        }
    }
}
