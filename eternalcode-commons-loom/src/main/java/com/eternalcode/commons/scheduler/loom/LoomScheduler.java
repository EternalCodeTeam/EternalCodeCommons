package com.eternalcode.commons.scheduler.loom;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Scheduler using Java 21 Virtual Threads.
 * <p>
 * Async methods run on VT (for IO).
 * Sync methods run on main thread (for Bukkit API).
 * <p>
 */
public interface LoomScheduler {


    LoomTask runAsync(Runnable task);

    <T> LoomFuture<T> supplyAsync(Supplier<T> supplier);

    <T> LoomFuture<T> callAsync(Callable<T> callable);

    LoomTask runAsyncLater(Runnable task, Duration delay);

    LoomTask runAsyncTimer(Runnable task, Duration delay, Duration period);

    LoomTask runSync(Runnable task);

    <T> LoomFuture<T> supplySync(Supplier<T> supplier);

    LoomTask runSyncLater(Runnable task, Duration delay);

    LoomTask runSyncTimer(Runnable task, Duration delay, Duration period);


    /**
     * Async work -> sync consumption. Common pattern shortcut.
     */
    <T> LoomTask runAsyncThenSync(Supplier<T> asyncSupplier, Consumer<T> syncConsumer);

    <T, R> LoomTask runAsyncThenSync(Supplier<T> asyncSupplier, Function<T, R> transformer, Consumer<R> syncConsumer);

    /**
     * Non-blocking delay. Does NOT hold any thread.
     */
    LoomFuture<Void> delay(Duration duration);

    boolean isMainThread();

    boolean shutdown(Duration timeout);

    void shutdownNow();
}
