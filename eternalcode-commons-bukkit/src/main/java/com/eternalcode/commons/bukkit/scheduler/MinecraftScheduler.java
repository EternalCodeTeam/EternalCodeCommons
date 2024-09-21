package com.eternalcode.commons.bukkit.scheduler;

import com.eternalcode.commons.scheduler.Scheduler;
import com.eternalcode.commons.scheduler.Task;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface MinecraftScheduler extends Scheduler {

    boolean isGlobalTickThread();

    boolean isPrimaryThread();

    boolean isRegionThread(Entity entity);

    boolean isRegionThread(Location location);

    Task run(Runnable task);

    Task runAsync(Runnable task);

    default Task run(Location location, Runnable task) {
        return run(task);
    }

    default Task run(Entity entity, Runnable task) {
        return run(task);
    }

    Task runLater(Runnable task, Duration delay);

    Task runLaterAsync(Runnable task, Duration delay);

    default Task runLater(Location location, Runnable task, Duration delay) {
        return runLater(task, delay);
    }

    default Task runLater(Entity entity, Runnable task, Duration delay) {
        return runLater(task, delay);
    }

    Task timer(Runnable task, Duration delay, Duration period);

    Task timerAsync(Runnable task, Duration delay, Duration period);

    default Task timer(Location location, Runnable task, Duration delay, Duration period) {
        return timer(task, delay, period);
    }

    default Task timer(Entity entity, Runnable task, Duration delay, Duration period) {
        return timer(task, delay, period);
    }

    <T> CompletableFuture<T> complete(Supplier<T> task);

    <T> CompletableFuture<T> completeAsync(Supplier<T> task);

    default <T> CompletableFuture<T> complete(Location location, Supplier<T> task) {
        return complete(task);
    }

    default <T> CompletableFuture<T> complete(Entity entity, Supplier<T> task) {
        return complete(task);
    }

}
