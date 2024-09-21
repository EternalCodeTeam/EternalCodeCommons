package com.eternalcode.commons.bukkit.scheduler;

import com.eternalcode.commons.scheduler.Scheduler;
import com.eternalcode.commons.scheduler.Task;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;
import java.util.function.Supplier;

public class BukkitSchedulerImpl implements MinecraftScheduler {

    private final Plugin plugin;
    private final Server server;
    private final BukkitScheduler rootScheduler;

    public BukkitSchedulerImpl(Plugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.rootScheduler = plugin.getServer().getScheduler();
    }

    @Override
    public boolean isGlobalTickThread() {
        return this.server.isPrimaryThread();
    }

    @Override
    public boolean isPrimaryThread() {
        return this.server.isPrimaryThread();
    }

    @Override
    public boolean isRegionThread(Entity entity) {
        return this.server.isPrimaryThread();
    }

    @Override
    public boolean isRegionThread(Location location) {
        return this.server.isPrimaryThread();
    }

    @Override
    public Task run(Runnable task) {
        return new BukkitTaskImpl(this.rootScheduler.runTask(this.plugin, task));
    }

    @Override
    public Task runAsync(Runnable task) {
        return new BukkitTaskImpl(this.rootScheduler.runTaskAsynchronously(this.plugin, task));
    }

    @Override
    public Task runLater(Runnable task, Duration delay) {
        return new BukkitTaskImpl(this.rootScheduler.runTaskLater(this.plugin, task, this.toTick(delay)));
    }

    @Override
    public Task runLaterAsync(Runnable task, Duration delay) {
        return new BukkitTaskImpl(this.rootScheduler.runTaskLaterAsynchronously(this.plugin, task, this.toTick(delay)));
    }

    @Override
    public Task timer(Runnable task, Duration delay, Duration period) {
        return new BukkitTaskImpl(this.rootScheduler.runTaskTimer(this.plugin, task, this.toTick(delay), this.toTick(period)), true);
    }

    @Override
    public Task timerAsync(Runnable task, Duration delay, Duration period) {
        return new BukkitTaskImpl(this.rootScheduler.runTaskTimerAsynchronously(this.plugin, task, this.toTick(delay), this.toTick(period)), true);
    }

    @Override
    public <T> CompletableFuture<T> complete(Supplier<T> task) {
        CompletableFuture<T> completable = new CompletableFuture<>();
        this.rootScheduler.runTask(this.plugin, () -> completable.complete(task.get()));
        return completable;
    }

    @Override
    public <T> CompletableFuture<T> completeAsync(Supplier<T> task) {
        CompletableFuture<T> completable = new CompletableFuture<>();
        this.rootScheduler.runTaskAsynchronously(this.plugin, () -> completable.complete(task.get()));
        return completable;
    }

    private long toTick(Duration duration) {
        return duration.toMillis() / 50L;
    }

}
