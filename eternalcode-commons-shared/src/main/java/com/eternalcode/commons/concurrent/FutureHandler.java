package com.eternalcode.commons.concurrent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FutureHandler<T> implements BiConsumer<T, Throwable> {

    private static final Logger LOGGER = Logger.getLogger(FutureHandler.class.getName());

    private final Consumer<T> success;

    private FutureHandler(Consumer<T> success) {
        this.success = success;
    }

    public static <T> FutureHandler<T> whenSuccess(Consumer<T> success) {
        return new FutureHandler<>(success);
    }

    public static <T> T handleException(Throwable cause) {
        LOGGER.log(
            Level.SEVERE,
            String.format("Caught an exception in future execution: %s", cause.getMessage()),
            cause);
        return null;
    }

    @Override
    public void accept(T value, Throwable cause) {
        if (cause != null) {
            LOGGER.log(Level.SEVERE, "Caught an exception in future execution: " + cause.getMessage(), cause);
            return;
        }

        success.accept(value);
    }
}
