package com.eternalcode.commons.scheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface Scheduler {

    boolean isGlobal();

    boolean isTick();

    boolean isEntity(Entity entity);

    boolean isRegion(Location location);

    Task sync(Runnable task);

    Task async(Runnable task);

    default Task async(Location location, Runnable task) {
        return async(task);
    }

    default Task async(Entity entity, Runnable task) {
        return async(task);
    }

    Task laterSync(Runnable task, Duration delay);

    Task laterAsync(Runnable task, Duration delay);

    default Task laterAsync(Location location, Runnable task, Duration delay) {
        return laterAsync(task, delay);
    }

    default Task laterAsync(Entity entity, Runnable task, Duration delay) {
        return laterAsync(task, delay);
    }

    Task timerSync(Runnable task, Duration delay, Duration period);

    Task timerAsync(Runnable task, Duration delay, Duration period);

    default Task timerAsync(Location location, Runnable task, Duration delay, Duration period) {
        return timerAsync(task, delay, period);
    }

    default Task timerAsync(Entity entity, Runnable task, Duration delay, Duration period) {
        return timerAsync(task, delay, period);
    }

    <T> CompletableFuture<T> completeSync(Supplier<T> task);

    <T> CompletableFuture<T> completeAsync(Supplier<T> task);
}
