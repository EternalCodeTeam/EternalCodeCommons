package com.eternalcode.commons.scheduler.loom;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Future wrapper with context switching between VT (async) and main thread
 * (sync).
 */
public class LoomFuture<T> implements LoomTask {

    private final CompletableFuture<T> future;
    private final MainThreadDispatcher dispatcher;
    private final VirtualThreadExecutor executor;
    private volatile boolean cancelled = false;

    LoomFuture(CompletableFuture<T> future, MainThreadDispatcher dispatcher, VirtualThreadExecutor executor) {
        this.future = future;
        this.dispatcher = dispatcher;
        this.executor = executor;
    }

    public <R> LoomFuture<R> thenApply(Function<? super T, ? extends R> mapper) {
        CompletableFuture<R> next = this.future.thenApplyAsync(mapper, this.executor.executor());
        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public LoomFuture<Void> thenAccept(Consumer<? super T> consumer) {
        CompletableFuture<Void> next = this.future.thenAcceptAsync(consumer, this.executor.executor());
        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public LoomFuture<Void> thenRun(Runnable action) {
        CompletableFuture<Void> next = this.future.thenRunAsync(action, this.executor.executor());
        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public <R> LoomFuture<R> thenCompose(Function<? super T, ? extends LoomFuture<R>> mapper) {
        CompletableFuture<R> next = this.future.thenComposeAsync(
            t -> mapper.apply(t).toCompletableFuture(),
            this.executor.executor());
        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public <R> LoomFuture<R> thenApplySync(Function<? super T, ? extends R> mapper) {
        CompletableFuture<R> next = new CompletableFuture<>();

        this.future.whenCompleteAsync(
            (result, error) -> {
                if (error != null) {
                    next.completeExceptionally(error);
                    return;
                }
                this.dispatcher.dispatch(() -> {
                    try {
                        next.complete(mapper.apply(result));
                    }
                    catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }, this.executor.executor());

        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public LoomFuture<Void> thenAcceptSync(Consumer<? super T> consumer) {
        CompletableFuture<Void> next = new CompletableFuture<>();

        this.future.whenCompleteAsync(
            (result, error) -> {
                if (error != null) {
                    next.completeExceptionally(error);
                    return;
                }
                this.dispatcher.dispatch(() -> {
                    try {
                        consumer.accept(result);
                        next.complete(null);
                    }
                    catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }, this.executor.executor());

        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public LoomFuture<Void> thenRunSync(Runnable action) {
        CompletableFuture<Void> next = new CompletableFuture<>();

        this.future.whenCompleteAsync(
            (result, error) -> {
                if (error != null) {
                    next.completeExceptionally(error);
                    return;
                }
                this.dispatcher.dispatch(() -> {
                    try {
                        action.run();
                        next.complete(null);
                    }
                    catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }, this.executor.executor());

        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }


    public LoomFuture<T> exceptionally(Consumer<Throwable> handler) {
        this.future.exceptionally(error -> {
            handler.accept(error);
            return null;
        });
        return this;
    }

    public LoomFuture<T> exceptionallyRecover(Function<Throwable, T> handler) {
        CompletableFuture<T> next = this.future.exceptionally(handler);
        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public LoomFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> handler) {
        this.future.whenCompleteAsync(handler, this.executor.executor());
        return this;
    }

    public LoomFuture<T> whenCompleteSync(BiConsumer<? super T, ? super Throwable> handler) {
        this.future.whenCompleteAsync(
            (result, error) -> {
                this.dispatcher.dispatch(() -> handler.accept(result, error));
            }, this.executor.executor());
        return this;
    }

    public LoomFuture<T> timeout(Duration timeout) {
        CompletableFuture<T> next = this.future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public LoomFuture<T> timeoutFallback(Duration timeout, T fallback) {
        CompletableFuture<T> next = this.future.completeOnTimeout(fallback, timeout.toMillis(), TimeUnit.MILLISECONDS);
        return new LoomFuture<>(next, this.dispatcher, this.executor);
    }

    public CompletableFuture<T> toCompletableFuture() {
        return this.future;
    }


    @Override
    public void cancel() {
        this.cancelled = true;
        this.future.cancel(true);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled || this.future.isCancelled();
    }

    @Override
    public boolean isRunning() {
        return !this.future.isDone() && !this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.future.isDone();
    }

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public boolean repeating() {
        return false;
    }

    @Override
    public java.util.concurrent.Future<?> asFuture() {
        return this.future;
    }
}
