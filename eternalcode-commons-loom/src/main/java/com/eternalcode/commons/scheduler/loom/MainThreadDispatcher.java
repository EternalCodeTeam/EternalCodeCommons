package com.eternalcode.commons.scheduler.loom;

/**
 * Dispatches tasks to main thread. Abstraction for platform independence.
 */
@FunctionalInterface
public interface MainThreadDispatcher {

    static MainThreadDispatcher synchronous() {
        return new MainThreadDispatcher() {
            @Override
            public void dispatch(Runnable task) {
                task.run();
            }

            @Override
            public boolean isMainThread() {
                return true;
            }

            @Override
            public void dispatchLater(Runnable task, long ticks) {
                task.run();
            }

            @Override
            public Cancellable dispatchTimer(Runnable task, long delay, long period) {
                task.run();
                return () -> {
                };
            }
        };
    }
    static QueuedDispatcher queued() {
        return new QueuedDispatcher();
    }
    default boolean isMainThread() {
        return false;
    }
    default void dispatchLater(Runnable task, long ticks) {
        dispatch(task);
    }
    default Cancellable dispatchTimer(Runnable task, long delay, long period) {
        throw new UnsupportedOperationException("dispatchTimer not implemented");
    }

    @FunctionalInterface
    interface Cancellable {
        void cancel();
    }
    void dispatch(Runnable task);
}
