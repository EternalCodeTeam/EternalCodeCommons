package com.eternalcode.commons.scheduler.loom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link LoomSchedulerImpl} using {@link QueuedDispatcher}
 * for controlled main thread simulation.
 */
@Timeout(10)
class LoomSchedulerImplTest {

    private QueuedDispatcher dispatcher;
    private LoomSchedulerImpl scheduler;

    @BeforeEach
    void setUp() {
        this.dispatcher = new QueuedDispatcher();
        this.scheduler = new LoomSchedulerImpl(this.dispatcher);
    }

    @AfterEach
    void tearDown() {
        this.scheduler.shutdownNow();
    }

    @Test
    @DisplayName("runAsync should execute task on virtual thread")
    void runAsync_executesTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        this.scheduler.runAsync(() -> {
            executed.set(true);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Task should complete");
        assertTrue(executed.get(), "Task should have executed");
    }

    @Test
    @DisplayName("supplyAsync should return value through future")
    void supplyAsync_returnsValue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> "hello")
            .thenAccept(value -> {
                result.set(value);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("hello", result.get());
    }

    @Test
    @DisplayName("thenApply should transform value on virtual thread")
    void thenApply_transformsValue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> 5)
            .thenApply(x -> x * 2)
            .thenApply(x -> x + 1)
            .thenAccept(value -> {
                result.set(value);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(11, result.get());
    }

    @Test
    @DisplayName("thenAcceptSync should queue task to main thread dispatcher")
    void thenAcceptSync_queuesToMainThread() throws InterruptedException {
        CountDownLatch asyncLatch = new CountDownLatch(1);
        AtomicBoolean syncExecuted = new AtomicBoolean(false);

        this.scheduler.supplyAsync(() -> {
                asyncLatch.countDown();
                return "data";
            })
            .thenAcceptSync(data -> {
                syncExecuted.set(true);
            });

        // Wait for async part to complete
        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS));

        // Give time for sync task to be queued
        Thread.sleep(100);

        // Sync task should NOT have executed yet (queued in dispatcher)
        assertFalse(syncExecuted.get(), "Sync task should be queued, not executed");
        assertEquals(1, this.dispatcher.getPendingCount(), "Should have 1 pending task");

        // Now drain the dispatcher (simulate main thread tick)
        this.dispatcher.runPending();

        assertTrue(syncExecuted.get(), "Sync task should have executed after drain");
    }

    @Test
    @DisplayName("thenApplySync should transform on main thread")
    void thenApplySync_transformsOnMainThread() throws InterruptedException {
        CountDownLatch asyncLatch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                asyncLatch.countDown();
                return 42;
            })
            .thenApplySync(number -> "Number: " + number)
            .thenAcceptSync(str -> result.set(str));

        // Wait for async and process sync queue
        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        // Drain twice (thenApplySync and thenAcceptSync)
        this.dispatcher.runPending();
        Thread.sleep(50);
        this.dispatcher.runPending();

        assertEquals("Number: 42", result.get());
    }

    @Test
    @DisplayName("runAsyncThenSync should complete full flow")
    void runAsyncThenSync_completesFlow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.runAsyncThenSync(
            () -> "async result",
            value -> {
                result.set(value);
                latch.countDown();
            });

        // Wait a bit then drain
        Thread.sleep(100);
        this.dispatcher.runPending();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("async result", result.get());
    }

    @Test
    @DisplayName("exceptionally should catch async exceptions")
    void exceptionally_catchesExceptions() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> caught = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                throw new RuntimeException("Test error");
            })
            .exceptionally(error -> {
                caught.set(error);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertNotNull(caught.get());
        assertTrue(caught.get().getMessage().contains("Test error"));
    }

    @Test
    @DisplayName("exceptionallyRecover should provide fallback value")
    void exceptionallyRecover_providesFallback() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                throw new RuntimeException("Error");
            })
            .exceptionallyRecover(error -> "fallback")
            .thenAccept(value -> {
                result.set((String) value);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("fallback", result.get());
    }

    @Test
    @DisplayName("cancel should prevent task execution")
    void cancel_preventsExecution() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);

        LoomTask task = this.scheduler.runAsyncLater(
            () -> {
                executed.set(true);
            }, Duration.ofSeconds(1));

        task.cancel();

        Thread.sleep(1500);

        assertTrue(task.isCancelled());
        assertFalse(executed.get(), "Cancelled task should not execute");
    }

    @Test
    @DisplayName("runSyncLater cancel should prevent dispatching to main thread")
    void runSyncLater_cancelPreventsDispatch() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);

        LoomTask task = this.scheduler.runSyncLater(
            () -> executed.set(true),
            Duration.ofMillis(300));

        task.cancel();
        Thread.sleep(450);
        this.dispatcher.runPending();

        assertTrue(task.isCancelled());
        assertFalse(executed.get(), "Cancelled sync task should not be dispatched");
    }

    @Test
    @DisplayName("delay should create non-blocking delay")
    void delay_createsNonBlockingDelay() throws InterruptedException {
        AtomicBoolean completed = new AtomicBoolean(false);
        long start = System.currentTimeMillis();

        this.scheduler.delay(Duration.ofMillis(200))
            .thenRun(() -> completed.set(true));

        // Should not block
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 100, "delay() should return immediately");

        // Wait for completion
        Thread.sleep(400);
        assertTrue(completed.get());
    }

    @Test
    @DisplayName("runAsyncTimer should execute repeatedly")
    void runAsyncTimer_executesRepeatedly() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);

        LoomTask task = this.scheduler.runAsyncTimer(
            () -> count.incrementAndGet(),
            Duration.ofMillis(50),
            Duration.ofMillis(50));

        Thread.sleep(300);
        task.cancel();

        int finalCount = count.get();
        assertTrue(finalCount >= 3, "Should have executed at least 3 times, got: " + finalCount);
        assertTrue(task.repeating());
    }

    @Test
    @DisplayName("shutdown should complete gracefully")
    void shutdown_completesGracefully() throws InterruptedException {
        AtomicInteger completed = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            this.scheduler.runAsync(() -> {
                try {
                    Thread.sleep(50);
                    completed.incrementAndGet();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        boolean success = this.scheduler.shutdown(Duration.ofSeconds(5));

        assertTrue(success, "Shutdown should complete");
        assertEquals(10, completed.get(), "All tasks should complete");
    }

    @Test
    @DisplayName("async tasks should run on virtual thread")
    void asyncTasks_runOnVirtualThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isVirtual = new AtomicBoolean(false);

        this.scheduler.runAsync(() -> {
            isVirtual.set(Thread.currentThread().isVirtual());
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(isVirtual.get(), "Task should run on virtual thread");
    }
}
