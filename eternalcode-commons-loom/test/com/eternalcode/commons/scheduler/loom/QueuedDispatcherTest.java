package com.eternalcode.commons.scheduler.loom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link QueuedDispatcher}.
 */
@Timeout(10)
class QueuedDispatcherTest {

    private QueuedDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        this.dispatcher = new QueuedDispatcher();
    }

    @Test
    @DisplayName("dispatch should queue task")
    void dispatch_queuesTask() {
        AtomicBoolean executed = new AtomicBoolean(false);

        this.dispatcher.dispatch(() -> executed.set(true));

        assertFalse(executed.get(), "Task should not execute immediately");
        assertEquals(1, this.dispatcher.getPendingCount());
        assertEquals(1, this.dispatcher.getDispatchCount());
        assertEquals(0, this.dispatcher.getExecuteCount());
    }

    @Test
    @DisplayName("runPending should execute all queued tasks")
    void runPending_executesAllTasks() {
        AtomicInteger counter = new AtomicInteger(0);

        this.dispatcher.dispatch(counter::incrementAndGet);
        this.dispatcher.dispatch(counter::incrementAndGet);
        this.dispatcher.dispatch(counter::incrementAndGet);

        int executed = this.dispatcher.runPending();

        assertEquals(3, executed);
        assertEquals(3, counter.get());
        assertEquals(0, this.dispatcher.getPendingCount());
    }

    @Test
    @DisplayName("runOne should execute exactly one task")
    void runOne_executesOneTask() {
        AtomicInteger counter = new AtomicInteger(0);

        this.dispatcher.dispatch(counter::incrementAndGet);
        this.dispatcher.dispatch(counter::incrementAndGet);
        this.dispatcher.dispatch(counter::incrementAndGet);

        boolean result1 = this.dispatcher.runOne();
        boolean result2 = this.dispatcher.runOne();

        assertTrue(result1);
        assertTrue(result2);
        assertEquals(2, counter.get());
        assertEquals(1, this.dispatcher.getPendingCount());
    }

    @Test
    @DisplayName("runOne should return false when empty")
    void runOne_returnsFalseWhenEmpty() {
        boolean result = this.dispatcher.runOne();

        assertFalse(result);
    }

    @Test
    @DisplayName("runUpTo should respect limit")
    void runUpTo_respectsLimit() {
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            this.dispatcher.dispatch(counter::incrementAndGet);
        }

        int executed = this.dispatcher.runUpTo(3);

        assertEquals(3, executed);
        assertEquals(3, counter.get());
        assertEquals(7, this.dispatcher.getPendingCount());
    }

    @Test
    @DisplayName("isMainThread should return false by default")
    void isMainThread_returnsFalseByDefault() {
        assertFalse(this.dispatcher.isMainThread());
    }

    @Test
    @DisplayName("isMainThread should return true for set thread")
    void isMainThread_returnsTrueForSetThread() {
        this.dispatcher.setMainThread(Thread.currentThread());

        assertTrue(this.dispatcher.isMainThread());
    }

    @Test
    @DisplayName("isMainThread should return false for different thread")
    void isMainThread_returnsFalseForDifferentThread() throws InterruptedException {
        Thread testThread = new Thread(() -> {
        });
        testThread.start();
        testThread.join();

        this.dispatcher.setMainThread(testThread);

        assertFalse(this.dispatcher.isMainThread());
    }

    @Test
    @DisplayName("dispatch should execute immediately when set and on main thread")
    void dispatch_executesImmediatelyWhenSet() {
        this.dispatcher.setMainThread(Thread.currentThread());
        this.dispatcher.setExecuteImmediatelyOnMainThread(true);

        AtomicBoolean executed = new AtomicBoolean(false);

        this.dispatcher.dispatch(() -> executed.set(true));

        assertTrue(executed.get(), "Task should execute immediately");
        assertEquals(0, this.dispatcher.getPendingCount());
        assertEquals(1, this.dispatcher.getExecuteCount());
    }

    @Test
    @DisplayName("dispatch should queue when not on main thread")
    void dispatch_queuesWhenNotOnMainThread() {
        this.dispatcher.setExecuteImmediatelyOnMainThread(true);
        // Main thread not set, so isMainThread returns false

        AtomicBoolean executed = new AtomicBoolean(false);

        this.dispatcher.dispatch(() -> executed.set(true));

        assertFalse(executed.get());
        assertEquals(1, this.dispatcher.getPendingCount());
    }

    @Test
    @DisplayName("counters should track correctly")
    void counters_trackCorrectly() {
        this.dispatcher.dispatch(() -> {
        });
        this.dispatcher.dispatch(() -> {
        });

        assertEquals(2, this.dispatcher.getDispatchCount());
        assertEquals(0, this.dispatcher.getExecuteCount());

        this.dispatcher.runOne();

        assertEquals(2, this.dispatcher.getDispatchCount());
        assertEquals(1, this.dispatcher.getExecuteCount());

        this.dispatcher.runPending();

        assertEquals(2, this.dispatcher.getDispatchCount());
        assertEquals(2, this.dispatcher.getExecuteCount());
    }

    @Test
    @DisplayName("hasPending should reflect queue state")
    void hasPending_reflectsQueueState() {
        assertFalse(this.dispatcher.hasPending());

        this.dispatcher.dispatch(() -> {
        });

        assertTrue(this.dispatcher.hasPending());

        this.dispatcher.runPending();

        assertFalse(this.dispatcher.hasPending());
    }

    @Test
    @DisplayName("clear should remove pending without executing")
    void clear_removesPendingWithoutExecuting() {
        AtomicBoolean executed = new AtomicBoolean(false);

        this.dispatcher.dispatch(() -> executed.set(true));
        this.dispatcher.dispatch(() -> executed.set(true));

        int cleared = this.dispatcher.clear();

        assertEquals(2, cleared);
        assertFalse(executed.get());
        assertEquals(0, this.dispatcher.getPendingCount());
    }

    @Test
    @DisplayName("reset should clear everything")
    void reset_clearsEverything() {
        this.dispatcher.dispatch(() -> {
        });
        this.dispatcher.dispatch(() -> {
        });
        this.dispatcher.runOne();

        this.dispatcher.reset();

        assertEquals(0, this.dispatcher.getPendingCount());
        assertEquals(0, this.dispatcher.getDispatchCount());
        assertEquals(0, this.dispatcher.getExecuteCount());
    }

    @Test
    @DisplayName("runPending should propagate exceptions")
    void runPending_propagatesExceptions() {
        this.dispatcher.dispatch(() -> {
            throw new RuntimeException("Test error");
        });

        assertThrows(RuntimeException.class, () -> this.dispatcher.runPending());
    }

    @Test
    @DisplayName("tasks should execute in order")
    void tasks_executeInOrder() {
        StringBuilder order = new StringBuilder();

        this.dispatcher.dispatch(() -> order.append("A"));
        this.dispatcher.dispatch(() -> order.append("B"));
        this.dispatcher.dispatch(() -> order.append("C"));

        this.dispatcher.runPending();

        assertEquals("ABC", order.toString());
    }

    @Test
    @DisplayName("dispatchLater should queue (simplified for testing)")
    void dispatchLater_queues() {
        AtomicBoolean executed = new AtomicBoolean(false);

        this.dispatcher.dispatchLater(() -> executed.set(true), 10);

        // In test mode, dispatchLater just queues
        this.dispatcher.runPending();

        assertTrue(executed.get());
    }
}
