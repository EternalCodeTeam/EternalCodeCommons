package com.eternalcode.commons;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

    private Supplier<T> supplier;
    private boolean initialized;
    private T value;
    private Exception exception;

    public Lazy(T value) {
        this.initialized = true;
        this.value = value;
    }

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static Lazy<Void> ofRunnable(Runnable runnable) {
        return new Lazy<>(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public synchronized T get() {
        if (exception != null) {
            throw new RuntimeException("Lazy value has been already initialized with exception", exception);
        }

        if (initialized) {
            return value;
        }

        this.initialized = true;

        try {
            return this.value = supplier.get();
        }
        catch (Exception exception) {
            this.exception = exception;
            throw new RuntimeException("Cannot initialize lazy value", exception);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean hasFailed() {
        return exception != null;
    }

}
