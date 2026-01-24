package com.eternalcode.commons.scheduler.loom;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps newVirtualThreadPerTaskExecutor for async tasks.
 * Uses ScheduledExecutorService only for delay timing - actual work goes to VT.
 */
public final class VirtualThreadExecutor {

    private final ExecutorService virtualExecutor;
    private final ScheduledExecutorService delayScheduler;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public VirtualThreadExecutor() {
        this.virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.delayScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LoomDelayTimer");
            t.setDaemon(true);
            return t;
        });
    }

    ExecutorService executor() {
        return this.virtualExecutor;
    }

    public Future<?> submit(Runnable task) {
        if (this.shutdown.get()) {
            return CompletableFuture.completedFuture(null);
        }
        return this.virtualExecutor.submit(task);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        if (this.shutdown.get()) {
            CompletableFuture<T> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("Executor shut down"));
            return f;
        }
        return this.virtualExecutor.submit(callable);
    }

    public DelayedTask submitDelayed(Runnable task, Duration delay) {
        if (this.shutdown.get()) {
            return DelayedTask.EMPTY;
        }

        AtomicBoolean cancelled = new AtomicBoolean(false);
        ScheduledFuture<?> sf = this.delayScheduler.schedule(
            () -> {
                if (!cancelled.get() && !this.shutdown.get()) {
                    this.virtualExecutor.submit(task);
                }
            }, delay.toMillis(), TimeUnit.MILLISECONDS);

        return new DelayedTask(sf, cancelled);
    }

    public DelayedTask submitRepeating(Runnable task, Duration delay, Duration period) {
        if (this.shutdown.get()) {
            return DelayedTask.EMPTY;
        }

        AtomicBoolean cancelled = new AtomicBoolean(false);
        ScheduledFuture<?> sf = this.delayScheduler.scheduleAtFixedRate(
            () -> {
                if (!cancelled.get() && !this.shutdown.get()) {
                    this.virtualExecutor.submit(task);
                }
            }, delay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);

        return new DelayedTask(sf, cancelled);
    }

    public CompletableFuture<Void> delay(Duration duration) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        this.delayScheduler.schedule(() -> f.complete(null), duration.toMillis(), TimeUnit.MILLISECONDS);
        return f;
    }

    public boolean isShutdown() {
        return this.shutdown.get();
    }

    public boolean shutdown(Duration timeout) {
        if (!this.shutdown.compareAndSet(false, true)) {
            return true;
        }

        this.delayScheduler.shutdown();
        this.virtualExecutor.shutdown();

        try {
            long half = timeout.toMillis() / 2;
            boolean a = this.delayScheduler.awaitTermination(half, TimeUnit.MILLISECONDS);
            boolean b = this.virtualExecutor.awaitTermination(half, TimeUnit.MILLISECONDS);
            return a && b;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void shutdownNow() {
        this.shutdown.set(true);
        this.delayScheduler.shutdownNow();
        this.virtualExecutor.shutdownNow();
    }

    public static final class DelayedTask {
        static final DelayedTask EMPTY = new DelayedTask(null, null);

        private final ScheduledFuture<?> future;
        private final AtomicBoolean cancelled;

        DelayedTask(ScheduledFuture<?> future, AtomicBoolean cancelled) {
            this.future = future;
            this.cancelled = cancelled;
        }

        public void cancel() {
            if (this.cancelled != null) {
                this.cancelled.set(true);
            }
            if (this.future != null) {
                this.future.cancel(false);
            }
        }

        public boolean isCancelled() {
            return (this.cancelled != null && this.cancelled.get())
                || (this.future != null && this.future.isCancelled());
        }

        public boolean isDone() {
            return this.future != null && this.future.isDone();
        }
    }
}
