package com.eternalcode.commons.scheduler.loom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link MainThreadDispatcher}.
 */
@Timeout(10)
class MainThreadDispatcherTest {

    @Test
    @DisplayName("synchronous dispatcher should execute immediately")
    void synchronous_executesImmediately() {
        MainThreadDispatcher dispatcher = MainThreadDispatcher.synchronous();

        AtomicBoolean executed = new AtomicBoolean(false);
        dispatcher.dispatch(() -> executed.set(true));

        assertTrue(executed.get());
    }

    @Test
    @DisplayName("synchronous dispatcher should report main thread")
    void synchronous_reportsMainThread() {
        MainThreadDispatcher dispatcher = MainThreadDispatcher.synchronous();

        assertTrue(dispatcher.isMainThread());
    }

    @Test
    @DisplayName("synchronous dispatcher dispatchLater should execute immediately")
    void synchronous_dispatchLater_executesImmediately() {
        MainThreadDispatcher dispatcher = MainThreadDispatcher.synchronous();

        AtomicBoolean executed = new AtomicBoolean(false);
        dispatcher.dispatchLater(() -> executed.set(true), 100);

        assertTrue(executed.get());
    }

    @Test
    @DisplayName("synchronous dispatcher dispatchTimer should execute once")
    void synchronous_dispatchTimer_executesOnce() {
        MainThreadDispatcher dispatcher = MainThreadDispatcher.synchronous();

        AtomicInteger count = new AtomicInteger(0);
        MainThreadDispatcher.Cancellable cancellable = dispatcher.dispatchTimer(count::incrementAndGet, 0, 1);

        assertEquals(1, count.get());
        assertNotNull(cancellable);
    }

    @Test
    @DisplayName("queued factory should return QueuedDispatcher")
    void queued_returnsQueuedDispatcher() {
        var dispatcher = MainThreadDispatcher.queued();

        assertNotNull(dispatcher);
        assertInstanceOf(QueuedDispatcher.class, dispatcher);
    }

    @Test
    @DisplayName("default isMainThread should return false")
    void default_isMainThread_returnsFalse() {
        MainThreadDispatcher dispatcher = task -> {
        };

        assertFalse(dispatcher.isMainThread());
    }

    @Test
    @DisplayName("custom implementation should work")
    void custom_implementation_works() {
        AtomicBoolean dispatched = new AtomicBoolean(false);

        MainThreadDispatcher dispatcher = task -> {
            dispatched.set(true);
            task.run();
        };

        dispatcher.dispatch(() -> {
        });

        assertTrue(dispatched.get());
    }

    @Test
    @DisplayName("Cancellable.cancel should be callable")
    void cancellable_cancel_callable() {
        AtomicBoolean cancelled = new AtomicBoolean(false);

        MainThreadDispatcher.Cancellable cancellable = () -> cancelled.set(true);
        cancellable.cancel();

        assertTrue(cancelled.get());
    }
}
