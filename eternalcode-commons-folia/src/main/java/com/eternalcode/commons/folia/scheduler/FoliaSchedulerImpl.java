package com.eternalcode.commons.folia.scheduler;

import com.eternalcode.commons.scheduler.Scheduler;
import com.eternalcode.commons.scheduler.Task;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class FoliaSchedulerImpl implements Scheduler {

    public final Plugin plugin;
    private final GlobalRegionScheduler globalRegionScheduler = Bukkit.getServer().getGlobalRegionScheduler();
    private final AsyncScheduler asyncScheduler = Bukkit.getServer().getAsyncScheduler();

    public FoliaSchedulerImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Task sync(Runnable task) {
        return new FoliaTaskImpl(this.globalRegionScheduler.run(plugin, runnable -> task.run()));
    }

    @Override
    public Task async(Runnable task) {
        return new FoliaTaskImpl(this.globalRegionScheduler.run(plugin, runnable -> task.run()));
    }

    @Override
    public Task laterSync(Runnable task, Duration delay) {
        return new FoliaTaskImpl(this.globalRegionScheduler.runDelayed(
            plugin,
            runnable -> task.run(),
            delay.toMillis())
        );
    }

    @Override
    public Task laterAsync(Runnable task, Duration delay) {
        return new FoliaTaskImpl(this.asyncScheduler.runDelayed(
            plugin,
            runnable -> task.run(),
            delay.toMillis(),
            TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public Task timerSync(Runnable task, Duration delay, Duration period) {
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
