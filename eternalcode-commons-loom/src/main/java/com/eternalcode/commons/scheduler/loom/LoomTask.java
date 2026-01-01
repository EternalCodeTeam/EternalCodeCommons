package com.eternalcode.commons.scheduler.loom;

import java.util.concurrent.Future;

/**
 * Handle for scheduled task. Provides cancellation and status.
 */
public interface LoomTask {

    LoomTask EMPTY = new LoomTask() {
        @Override
        public void cancel() {
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public boolean async() {
            return false;
        }

        @Override
        public boolean repeating() {
            return false;
        }

        @Override
        public Future<?> asFuture() {
            return null;
        }
    };
    void cancel();
    boolean isCancelled();
    boolean isRunning();
    boolean isDone();
    boolean async();
    boolean repeating();
    Future<?> asFuture();
}
