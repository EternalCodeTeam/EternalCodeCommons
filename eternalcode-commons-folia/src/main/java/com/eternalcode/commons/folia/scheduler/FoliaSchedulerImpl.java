package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.bukkit.scheduler.MinecraftScheduler;
import com.eternalcode.commons.scheduler.Scheduler;
import com.eternalcode.commons.scheduler.Task;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class FoliaSchedulerImpl implements MinecraftScheduler {

    public final Plugin plugin;
    private final Server server;

    private final GlobalRegionScheduler globalRegionScheduler;
    private final AsyncScheduler asyncScheduler;
    private final RegionScheduler regionScheduler;

    public FoliaSchedulerImpl(Plugin plugin) {
        this.plugin = plugin;
        this.server = this.plugin.getServer();

        this.globalRegionScheduler = this.server.getGlobalRegionScheduler();
        this.asyncScheduler = this.server.getAsyncScheduler();
        this.regionScheduler = this.server.getRegionScheduler();
        this.server.getScheduler();
    }

    @Override
    public boolean isGlobalTickThread() {
        return this.server.isGlobalTickThread();
    }

    @Override
    public boolean isPrimaryThread() {
        return this.server.isPrimaryThread();
    }

    @Override
    public boolean isRegionThread(Entity entity) {
        return this.server.isOwnedByCurrentRegion(entity);
    }

    @Override
    public boolean isRegionThread(Location location) {
        return this.server.isOwnedByCurrentRegion(location);
    }

    @Override
    public Task run(Runnable task) {
        return wrap(this.globalRegionScheduler.run(plugin, runnable -> task.run()));
    }

    @Override
    public Task runAsync(Runnable task) {
        return wrapAsync(this.asyncScheduler.runNow(plugin, runnable -> task.run()));
    }

    @Override
    public Task run(Location location, Runnable task) {
        return wrap(this.regionScheduler.run(plugin, location, runnable -> task.run()));
    }

    @Override
    public Task run(Entity entity, Runnable task) {
        return wrap(entity.getScheduler().run(plugin, runnable -> task.run(), null));
    }

    @Override
    public Task runLater(Runnable task, Duration delay) {
        return wrap(this.globalRegionScheduler.runDelayed(plugin, scheduledTask -> task.run(), toTick(delay)));
    }

    @Override
    public Task runLaterAsync(Runnable task, Duration delay) {
        return wrapAsync(this.asyncScheduler.runDelayed(
            plugin,
            runnable -> task.run(),
            delay.toMillis(),
            TimeUnit.MILLISECONDS
        ));
    }

    @Override
    public Task runLater(Location location, Runnable task, Duration delay) {
        return wrap(this.regionScheduler.runDelayed(
            plugin,
            location,
            runnable -> task.run(),
            toTick(delay)
        ));
    }

    @Override
    public Task runLater(Entity entity, Runnable task, Duration delay) {
        return wrap(entity.getScheduler().runDelayed(
            plugin,
            runnable -> task.run(),
            null,
            toTick(delay)
        ));
    }

    @Override
    public Task timer(Runnable task, Duration delay, Duration period) {
        return wrap(this.globalRegionScheduler.runAtFixedRate(
            plugin,
            runnable -> task.run(),
            toTick(delay),
            toTick(period)
        ));
    }

    @Override
    public Task timerAsync(Runnable task, Duration delay, Duration period) {
        return wrapAsync(this.asyncScheduler.runAtFixedRate(
            plugin,
            runnable -> task.run(),
            delay.toMillis(),
            period.toMillis(),
            TimeUnit.MILLISECONDS
        ));
    }

    @Override
    public Task timer(Location location, Runnable task, Duration delay, Duration period) {
        return wrap(this.regionScheduler.runAtFixedRate(
            plugin,
            location,
            runnable -> task.run(),
            toTick(delay),
            toTick(period)
        ));
    }

    @Override
    public Task timer(Entity entity, Runnable task, Duration delay, Duration period) {
        return wrap(entity.getScheduler().runAtFixedRate(
            plugin,
            runnable -> task.run(),
            null,
            toTick(delay),
            toTick(period)
        ));
    }

    @Override
    public <T> CompletableFuture<T> complete(Supplier<T> task) {
        CompletableFuture<T> completable = new CompletableFuture<>();
        this.globalRegionScheduler.run(plugin, scheduledTask -> completable.complete(task.get()));
        return completable;
    }

    @Override
    public <T> CompletableFuture<T> completeAsync(Supplier<T> task) {
        CompletableFuture<T> completable = new CompletableFuture<>();
        this.asyncScheduler.runNow(plugin, scheduledTask -> completable.complete(task.get()));
        return completable;
    }

    @Override
    public <T> CompletableFuture<T> complete(Location location, Supplier<T> task) {
        CompletableFuture<T> completable = new CompletableFuture<>();
        this.regionScheduler.run(plugin, location, scheduledTask -> completable.complete(task.get()));
        return completable;
    }

    @Override
    public <T> CompletableFuture<T> complete(Entity entity, Supplier<T> task) {
        CompletableFuture<T> completable = new CompletableFuture<>();
        entity.getScheduler().run(plugin, scheduledTask -> completable.complete(task.get()), null);
        return completable;
    }

    private long toTick(Duration duration) {
        return duration.toMillis() / 50L;
    }

    private Task wrap(ScheduledTask task) {
        return new FoliaTaskImpl(task);
    }

    private Task wrapAsync(ScheduledTask task) {
        return new FoliaTaskImpl(task, true);
    }

}
