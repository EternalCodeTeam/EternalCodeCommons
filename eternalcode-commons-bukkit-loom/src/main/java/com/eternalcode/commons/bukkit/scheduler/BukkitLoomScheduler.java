package com.eternalcode.commons.bukkit.scheduler;

import com.eternalcode.commons.scheduler.loom.LoomFuture;
import com.eternalcode.commons.scheduler.loom.LoomScheduler;
import com.eternalcode.commons.scheduler.loom.LoomSchedulerImpl;
import com.eternalcode.commons.scheduler.loom.LoomTask;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Bukkit wrapper for LoomScheduler.
 * Create in onEnable(), shutdown in onDisable().
 */
public final class BukkitLoomScheduler implements LoomScheduler {

    private final BukkitMainThreadDispatcher dispatcher;
    private final LoomSchedulerImpl delegate;
    private final Plugin plugin;

    private BukkitLoomScheduler(Plugin plugin, BukkitMainThreadDispatcher dispatcher) {
        this.plugin = plugin;
        this.dispatcher = dispatcher;
        this.delegate = new LoomSchedulerImpl(dispatcher);
    }

    public static BukkitLoomScheduler create(Plugin plugin) {
        if (!plugin.getServer().isPrimaryThread()) {
            throw new IllegalStateException("BukkitLoomScheduler must be created on main thread");
        }
        return new BukkitLoomScheduler(plugin, new BukkitMainThreadDispatcher(plugin));
    }

    @Override
    public LoomTask runAsync(Runnable task) {
        return this.delegate.runAsync(task);
    }

    @Override
    public <T> LoomFuture<T> supplyAsync(Supplier<T> supplier) {
        return this.delegate.supplyAsync(supplier);
    }

    @Override
    public <T> LoomFuture<T> callAsync(Callable<T> callable) {
        return this.delegate.callAsync(callable);
    }

    @Override
    public LoomTask runAsyncLater(Runnable task, Duration delay) {
        return this.delegate.runAsyncLater(task, delay);
    }

    @Override
    public LoomTask runAsyncTimer(Runnable task, Duration delay, Duration period) {
        return this.delegate.runAsyncTimer(task, delay, period);
    }

    @Override
    public LoomTask runSync(Runnable task) {
        return this.delegate.runSync(task);
    }

    @Override
    public <T> LoomFuture<T> supplySync(Supplier<T> supplier) {
        return this.delegate.supplySync(supplier);
    }

    @Override
    public LoomTask runSyncLater(Runnable task, Duration delay) {
        return this.delegate.runSyncLater(task, delay);
    }

    @Override
    public LoomTask runSyncTimer(Runnable task, Duration delay, Duration period) {
        return this.delegate.runSyncTimer(task, delay, period);
    }

    @Override
    public <T> LoomTask runAsyncThenSync(Supplier<T> asyncSupplier, Consumer<T> syncConsumer) {
        return this.delegate.runAsyncThenSync(asyncSupplier, syncConsumer);
    }

    @Override
    public <T, R> LoomTask runAsyncThenSync(Supplier<T> asyncSupplier, Function<T, R> transformer,
            Consumer<R> syncConsumer) {
        return this.delegate.runAsyncThenSync(asyncSupplier, transformer, syncConsumer);
    }

    @Override
    public LoomFuture<Void> delay(Duration duration) {
        return this.delegate.delay(duration);
    }

    @Override
    public boolean isMainThread() {
        return this.dispatcher.isMainThread();
    }

    @Override
    public boolean shutdown(Duration timeout) {
        this.dispatcher.shutdown();
        return this.delegate.shutdown(timeout);
    }

    @Override
    public void shutdownNow() {
        this.dispatcher.shutdown();
        this.delegate.shutdownNow();
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public int getPendingSyncTasks() {
        return this.dispatcher.getPendingCount();
    }
}
