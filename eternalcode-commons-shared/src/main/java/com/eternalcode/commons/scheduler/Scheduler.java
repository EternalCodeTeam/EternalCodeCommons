package com.eternalcode.commons.scheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Scheduler {

    Task run(Runnable task);

    Task runAsync(Runnable task);

    Task runLater(Runnable task, Duration delay);

    Task runLaterAsync(Runnable task, Duration delay);

    Task timer(Runnable task, Duration delay, Duration period);

    Task timerAsync(Runnable task, Duration delay, Duration period);

    <T> CompletableFuture<T> complete(Supplier<T> task);

    <T> CompletableFuture<T> completeAsync(Supplier<T> task);

}
