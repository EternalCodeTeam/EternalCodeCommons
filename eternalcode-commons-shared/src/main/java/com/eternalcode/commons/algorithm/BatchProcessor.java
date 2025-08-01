package com.eternalcode.commons.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility for processing collections in batches.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Batch_processing">Batch processing - Wikipedia</a>
 */
public class BatchProcessor<T> {

    private final List<T> elements;
    private final int batchSize;
    private int position;

    /**
     * Creates a new batch processor.
     *
     * @param elements  collection to process
     * @param batchSize size of each batch (must be > 0)
     */
    public BatchProcessor(Collection<T> elements, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be greater than 0");
        }

        this.elements = new ArrayList<>(elements);
        this.batchSize = batchSize;
        this.position = 0;
    }

    /**
     * Processes the next batch of elements.
     *
     * @param processor action to apply to each element
     * @return true if more elements remain, false if all processed
     */
    public boolean processNext(Consumer<T> processor) {
        if (position >= elements.size()) {
            return false;
        }

        int endPosition = Math.min(position + batchSize, elements.size());
        List<T> currentBatch = elements.subList(position, endPosition);

        currentBatch.forEach(processor);

        position = endPosition;
        return position < elements.size();
    }

    /**
     * Resets processor to start from beginning.
     */
    public void reset() {
        position = 0;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getPosition() {
        return position;
    }

    public int getTotalSize() {
        return elements.size();
    }

    public boolean isComplete() {
        return position >= elements.size();
    }

    public int getRemainingCount() {
        return Math.max(0, elements.size() - position);
    }

    public int getProcessedBatchCount() {
        return (position + batchSize - 1) / batchSize;
    }

    public int getTotalBatchCount() {
        return (elements.size() + batchSize - 1) / batchSize;
    }

    public double getProgress() {
        if (elements.isEmpty()) {
            return 1.0;
        }
        return (double) position / elements.size();
    }
}
