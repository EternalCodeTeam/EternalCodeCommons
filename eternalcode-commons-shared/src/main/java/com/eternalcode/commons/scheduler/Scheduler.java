package com.eternalcode.commons.scheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Scheduler {

    Task sync(Runnable task);

    Task async(Runnable task);

    Task laterSync(Runnable task, Duration delay);

    Task laterAsync(Runnable task, Duration delay);

    Task timerSync(Runnable task, Duration delay, Duration period);

    Task timerAsync(Runnable task, Duration delay, Duration period);

    <T> CompletableFuture<T> completeSync(Supplier<T> task);

    <T> CompletableFuture<T> completeAsync(Supplier<T> task);
}
