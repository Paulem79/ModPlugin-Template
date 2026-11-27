package com.example.template.common.destruction.state;

import com.example.template.common.destruction.DestructionContext;
import com.example.template.common.destruction.model.DestroyedBlock;
import com.example.template.common.destruction.model.StoredEntity;

import java.util.List;

/** Restores blocks (last destroyed first), then entities, within a time budget per tick. */
public class RestoringState<W, K, MD extends DestroyedBlock, ME extends StoredEntity>
        implements DestructionState<DestructionContext<W, K, MD, ME>> {
    private int blockIndex;
    private int entityIndex;

    @Override
    public void enter(DestructionContext<W, K, MD, ME> context) {
        this.blockIndex = context.getDestroyedBlocks().size() - 1;
        this.entityIndex = context.getStoredEntities().size() - 1;
    }

    @Override
    public DestructionState<DestructionContext<W, K, MD, ME>> tick(DestructionContext<W, K, MD, ME> context, long startTime) {
        List<MD> destroyedBlocks = context.getDestroyedBlocks();
        List<ME> storedEntities = context.getStoredEntities();

        while (blockIndex >= 0) {
            context.restoreBlock(destroyedBlocks.get(blockIndex--));

            if (context.isOutOfBudget(startTime)) {
                return this;
            }
        }

        while (entityIndex >= 0) {
            context.restoreEntity(storedEntities.get(entityIndex--));

            if (context.isOutOfBudget(startTime)) {
                return this;
            }
        }

        destroyedBlocks.clear();
        storedEntities.clear();
        return null;
    }
}
