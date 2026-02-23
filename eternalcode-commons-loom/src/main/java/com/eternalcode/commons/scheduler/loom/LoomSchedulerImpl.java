package com.eternalcode.commons.scheduler.loom;

import com.eternalcode.commons.scheduler.loom.VirtualThreadExecutor.DelayedTask;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * LoomScheduler implementation using Virtual Threads.
 */
public class LoomSchedulerImpl implements LoomScheduler {

    private final VirtualThreadExecutor vtExecutor;
    private final MainThreadDispatcher dispatcher;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public LoomSchedulerImpl(MainThreadDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.vtExecutor = new VirtualThreadExecutor();
    }

    public LoomSchedulerImpl(MainThreadDispatcher dispatcher, VirtualThreadExecutor vtExecutor) {
        this.dispatcher = dispatcher;
        this.vtExecutor = vtExecutor;
    }

    @Override
    public LoomTask runAsync(Runnable task) {
        if (this.shutdown.get()) {
            return LoomTask.EMPTY;
        }
        Future<?> future = this.vtExecutor.submit(task);
        return new SimpleLoomTask(future, true, false);
    }

    @Override
    public <T> LoomFuture<T> supplyAsync(Supplier<T> supplier) {
        if (this.shutdown.get()) {
            CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("Scheduler shut down"));
            return new LoomFuture<>(failed, this.dispatcher, this.vtExecutor);
        }
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, this.vtExecutor.executor());
        return new LoomFuture<>(future, this.dispatcher, this.vtExecutor);
    }

    @Override
    public <T> LoomFuture<T> callAsync(Callable<T> callable) {
        if (this.shutdown.get()) {
            CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("Scheduler shut down"));
            return new LoomFuture<>(failed, this.dispatcher, this.vtExecutor);
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        this.vtExecutor.submit(() -> {
            try {
                future.complete(callable.call());
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return new LoomFuture<>(future, this.dispatcher, this.vtExecutor);
    }

    @Override
    public LoomTask runAsyncLater(Runnable task, Duration delay) {
        if (this.shutdown.get()) {
            return LoomTask.EMPTY;
        }
        VirtualThreadExecutor.DelayedTask dt = this.vtExecutor.submitDelayed(task, delay);
        return new DelayedLoomTask(dt, true, false);
    }

    @Override
    public LoomTask runAsyncTimer(Runnable task, Duration delay, Duration period) {
        if (this.shutdown.get()) {
            return LoomTask.EMPTY;
        }
        VirtualThreadExecutor.DelayedTask dt = this.vtExecutor.submitRepeating(task, delay, period);
        return new DelayedLoomTask(dt, true, true);
    }

    @Override
    public LoomTask runSync(Runnable task) {
        if (this.shutdown.get()) {
            return LoomTask.EMPTY;
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.dispatcher.dispatch(() -> {
            try {
                task.run();
                future.complete(null);
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return new SimpleLoomTask(future, false, false);
    }

    @Override
    public <T> LoomFuture<T> supplySync(Supplier<T> supplier) {
        if (this.shutdown.get()) {
            CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("Scheduler shut down"));
            return new LoomFuture<>(failed, this.dispatcher, this.vtExecutor);
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        this.dispatcher.dispatch(() -> {
            try {
                future.complete(supplier.get());
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return new LoomFuture<>(future, this.dispatcher, this.vtExecutor);
    }

    @Override
    public LoomTask runSyncLater(Runnable task, Duration delay) {
        if (this.shutdown.get()) {
            return LoomTask.EMPTY;
        }
        DelayedTask dt = this.vtExecutor.submitDelayed(() -> this.dispatcher.dispatch(task), delay);
        return new DelayedLoomTask(dt, false, false);
    }

    @Override
    public LoomTask runSyncTimer(Runnable task, Duration delay, Duration period) {
        if (this.shutdown.get()) {
            return LoomTask.EMPTY;
        }
        DelayedTask dt = this.vtExecutor.submitRepeating(() -> this.dispatcher.dispatch(task), delay, period);
        return new DelayedLoomTask(dt, false, true);
    }

    @Override
    public <T> LoomTask runAsyncThenSync(Supplier<T> asyncSupplier, Consumer<T> syncConsumer) {
        return supplyAsync(asyncSupplier).thenAcceptSync(syncConsumer);
    }

    @Override
    public <T, R> LoomTask runAsyncThenSync(
        Supplier<T> asyncSupplier, Function<T, R> transformer,
        Consumer<R> syncConsumer) {
        return supplyAsync(asyncSupplier).thenApply(transformer).thenAcceptSync(syncConsumer);
    }

    @Override
    public LoomFuture<Void> delay(Duration duration) {
        return new LoomFuture<>(this.vtExecutor.delay(duration), this.dispatcher, this.vtExecutor);
    }

    @Override
    public boolean isMainThread() {
        return this.dispatcher.isMainThread();
    }

    @Override
    public boolean shutdown(Duration timeout) {
        if (!this.shutdown.compareAndSet(false, true)) {
            return true;
        }
        return this.vtExecutor.shutdown(timeout);
    }

    @Override
    public void shutdownNow() {
        this.shutdown.set(true);
        this.vtExecutor.shutdownNow();
    }

    private static final class SimpleLoomTask implements LoomTask {
        private final Future<?> future;
        private final boolean async;
        private final boolean repeating;
        private volatile boolean cancelled = false;

        SimpleLoomTask(Future<?> future, boolean async, boolean repeating) {
            this.future = future;
            this.async = async;
            this.repeating = repeating;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
            if (this.future != null) {
                this.future.cancel(true);
            }
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled || (this.future != null && this.future.isCancelled());
        }

        @Override
        public boolean isRunning() {
            return !isDone() && !isCancelled();
        }

        @Override
        public boolean isDone() {
            return this.future != null && this.future.isDone();
        }

        @Override
        public boolean async() {
            return this.async;
        }

        @Override
        public boolean repeating() {
            return this.repeating;
        }

        @Override
        public Future<?> asFuture() {
            return this.future;
        }
    }

    private record DelayedLoomTask(DelayedTask task, boolean async, boolean repeating) implements LoomTask {

        @Override
            public void cancel() {
                this.task.cancel();
            }

            @Override
            public boolean isCancelled() {
                return this.task.isCancelled();
            }

            @Override
            public boolean isRunning() {
                return !isDone() && !isCancelled();
            }

            @Override
            public boolean isDone() {
                return this.task.isDone();
            }

            @Override
            public Future<?> asFuture() {
                return null;
            }
        }

}
