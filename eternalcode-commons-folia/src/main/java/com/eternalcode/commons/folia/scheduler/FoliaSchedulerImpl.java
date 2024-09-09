package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.Scheduler;
import com.eternalcode.commons.scheduler.Task;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class FoliaSchedulerImpl implements Scheduler {

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
    }

    @Override
    public boolean isGlobal() {
        return this.server.isGlobalTickThread();
    }

    @Override
    public boolean isTick() {
        return this.server.isPrimaryThread();
    }

    @Override
    public boolean isEntity(Entity entity) {
        return this.server.isOwnedByCurrentRegion(entity);
    }

    @Override
    public boolean isRegion(Location location) {
        return this.server.isOwnedByCurrentRegion(location);
    }

    @Override
    public Task sync(Runnable task) {
        return new FoliaTaskImpl(this.globalRegionScheduler.run(plugin, runnable -> task.run()));
    }

    @Override
    public Task async(Runnable task) {
        return new FoliaTaskImpl(this.asyncScheduler.runNow(plugin, runnable -> task.run()));
    }

    @Override
    public Task async(Location location, Runnable task) {
        return new FoliaTaskImpl(this.regionScheduler.run(plugin, location, runnable -> task.run()));
    }

    @Override
    public Task async(Entity entity, Runnable task) {
        return new FoliaTaskImpl(entity.getScheduler().run(plugin, runnable -> task.run(), null));
    }

    @Override
    public Task laterSync(Runnable task, Duration delay) {
        if (delay.isZero()) {
            delay = Duration.ofMillis(1);
        }

        return new FoliaTaskImpl(this.globalRegionScheduler.runDelayed(
            plugin,
            runnable -> task.run(),
            delay.toMillis())
        );
    }

    @Override
    public Task laterAsync(Runnable task, Duration delay) {
        return new FoliaTaskImpl(this.globalRegionScheduler.runDelayed(
            plugin,
            runnable -> task.run(),
            delay.toMillis())
        );
    }

    @Override
    public Task laterAsync(Location location, Runnable task, Duration delay) {
        return new FoliaTaskImpl(this.regionScheduler.runDelayed(
            plugin,
            location,
            runnable -> task.run(),
            delay.toMillis())
        );
    }

    @Override
    public Task laterAsync(Entity entity, Runnable task, Duration delay) {
        return new FoliaTaskImpl(entity.getScheduler().runDelayed(
            plugin,
            runnable -> task.run(),
            null,
            delay.toMillis())
        );
    }

    @Override
    public Task timerSync(Runnable task, Duration delay, Duration period) {
        if (delay.isZero()) {
            delay = Duration.ofMillis(1);
        }

        if (period.isZero()) {
            period = Duration.ofMillis(1);
        }

        return new FoliaTaskImpl(this.globalRegionScheduler.runAtFixedRate(
            plugin,
            runnable -> task.run(),
            delay.toMillis(),
            period.toMillis())
        );
    }

    @Override
    public Task timerAsync(Runnable task, Duration delay, Duration period) {
        return new FoliaTaskImpl(this.asyncScheduler.runAtFixedRate(
            plugin,
            runnable -> task.run(),
            delay.toMillis(),
            period.toMillis(),
            TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public Task timerAsync(Location location, Runnable task, Duration delay, Duration period) {
        return new FoliaTaskImpl(this.regionScheduler.runAtFixedRate(
            plugin,
            location,
            runnable -> task.run(),
            delay.toMillis(),
            period.toMillis())
        );
    }

    @Override
    public Task timerAsync(Entity entity, Runnable task, Duration delay, Duration period) {
        return new FoliaTaskImpl(entity.getScheduler().runAtFixedRate(
            plugin,
            runnable -> task.run(),
            null,
            delay.toMillis(),
            period.toMillis())
        );
    }

    @Override
    public <T> CompletableFuture<T> completeSync(Supplier<T> task) {
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
}
