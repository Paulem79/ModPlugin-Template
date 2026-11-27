package com.example.template.common.destruction;

import com.example.template.common.TemplateSettings;
import com.example.template.common.destruction.model.DestroyedBlock;
import com.example.template.common.destruction.model.StoredEntity;
import com.example.template.common.utils.AlphabetUtils;

import java.util.*;

/**
 * State shared by all steps of one destruction, plus the platform hooks they rely on.
 *
 * @param <W>  world type
 * @param <K>  chunk type
 * @param <MD> destroyed block model
 * @param <ME> stored entity model
 */
public abstract class DestructionContext<W, K, MD extends DestroyedBlock, ME extends StoredEntity> {
    private final W world;
    private final char letter;
    private final long maxNanosPerTick;

    private final Set<Long> queuedChunkKeys = new HashSet<>();
    private final List<K> targetChunks = new ArrayList<>();
    private final List<MD> destroyedBlocks = new ArrayList<>();
    private final List<ME> storedEntities = new ArrayList<>();
    private final Map<Long, Integer> chunkStartHeights = new HashMap<>();

    private int maxStartHeight = Integer.MIN_VALUE;

    protected DestructionContext(W world, char letter, long maxNanosPerTick) {
        this.world = world;
        this.letter = letter;
        this.maxNanosPerTick = maxNanosPerTick;
    }

    protected DestructionContext(W world, char letter) {
        this(world, letter, TemplateSettings.config().maxNanosPerTick());
    }

    /** Unique key of a chunk, used to track per-chunk data. */
    public abstract long chunkKey(K chunk);

    public abstract int minY();

    /** Highest Y from which blocks are destroyed. */
    public abstract int topY();

    public abstract void announceDestruction();

    /** Computes the area to destroy around each player of the world, spectators excluded. */
    public abstract List<PlayerArea<K>> collectPlayerAreas();

    /** Stores the non-player entities of the chunk in {@link #getStoredEntities()}, then removes them. */
    public abstract void storeAndRemoveEntities(K chunk);

    /** Destroys the block if needed and records it in {@link #getDestroyedBlocks()}. */
    public abstract void destroyBlockAt(K chunk, int x, int y, int z);

    public abstract void restoreBlock(MD block);

    public abstract void restoreEntity(ME entity);

    /** A block must be destroyed unless one of its words starts with the current letter. */
    public boolean shouldDestroy(String blockName) {
        return !AlphabetUtils.initials(blockName).contains(letter);
    }

    public boolean addChunk(K chunk) {
        if (queuedChunkKeys.add(chunkKey(chunk))) {
            targetChunks.add(chunk);
            return true;
        }
        return false;
    }

    public char getLetter() {
        return letter;
    }

    public long getMaxNanosPerTick() {
        return maxNanosPerTick;
    }

    public boolean isOutOfBudget(long startTime) {
        return System.nanoTime() - startTime >= maxNanosPerTick;
    }

    public Map<Long, Integer> getChunkStartHeights() {
        return chunkStartHeights;
    }

    public int getMaxStartHeight() {
        return maxStartHeight;
    }

    public void setMaxStartHeight(int maxStartHeight) {
        this.maxStartHeight = maxStartHeight;
    }

    public List<MD> getDestroyedBlocks() {
        return destroyedBlocks;
    }

    public List<ME> getStoredEntities() {
        return storedEntities;
    }

    public List<K> getTargetChunks() {
        return targetChunks;
    }

    public W getWorld() {
        return world;
    }
}
