package com.eternalcode.commons.scheduler.loom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
 * Tests for {@link VirtualThreadExecutor}.
 */
@Timeout(10)
class VirtualThreadExecutorTest {

    private VirtualThreadExecutor executor;

    @BeforeEach
    void setUp() {
        this.executor = new VirtualThreadExecutor();
    }

    @AfterEach
    void tearDown() {
        this.executor.shutdownNow();
    }

    @Test
    @DisplayName("submit Runnable should execute on virtual thread")
    void submit_runnable_executesOnVirtualThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isVirtual = new AtomicBoolean(false);

        this.executor.submit(() -> {
            isVirtual.set(Thread.currentThread().isVirtual());
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(isVirtual.get(), "Task should run on virtual thread");
    }

    @Test
    @DisplayName("submit Callable should return result")
    void submit_callable_returnsResult() throws Exception {
        var future = this.executor.submit(() -> "hello world");

        // Wait for result in separate VT to avoid blocking
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.executor.submit(() -> {
            try {
                result.set(future.get());
            }
            catch (Exception e) {
                fail(e);
            }
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("hello world", result.get());
    }

    @Test
    @DisplayName("submit should handle multiple concurrent tasks")
    void submit_handlesConcurrentTasks() throws InterruptedException {
        int taskCount = 100;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            this.executor.submit(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(taskCount, counter.get());
    }

    @Test
    @DisplayName("submitDelayed should execute after delay")
    void submitDelayed_executesAfterDelay() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        long start = System.currentTimeMillis();

        this.executor.submitDelayed(
            () -> {
                executed.set(true);
            }, Duration.ofMillis(200));

        assertFalse(executed.get());

        Thread.sleep(350);

        assertTrue(executed.get(), "Task should have executed after delay");
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 200, "Should have waited at least 200ms");
    }

    @Test
    @DisplayName("submitDelayed should be cancellable")
    void submitDelayed_isCancellable() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);

        var task = this.executor.submitDelayed(
            () -> {
                executed.set(true);
            }, Duration.ofSeconds(1));

        task.cancel();

        Thread.sleep(1500);

        assertFalse(executed.get(), "Cancelled task should not execute");
        assertTrue(task.isCancelled());
    }

    @Test
    @DisplayName("submitRepeating should execute multiple times")
    void submitRepeating_executesMultipleTimes() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);

        var task = this.executor.submitRepeating(
            count::incrementAndGet,
            Duration.ofMillis(50),
            Duration.ofMillis(50));

        Thread.sleep(300);
        task.cancel();

        int finalCount = count.get();
        assertTrue(finalCount >= 4, "Should have executed at least 4 times, got: " + finalCount);
    }

    @Test
    @DisplayName("submitRepeating should stop after cancel")
    void submitRepeating_stopsAfterCancel() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);

        var task = this.executor.submitRepeating(
            count::incrementAndGet,
            Duration.ofMillis(50),
            Duration.ofMillis(50));

        Thread.sleep(200);
        task.cancel();
        int countAfterCancel = count.get();

        Thread.sleep(200);

        assertEquals(countAfterCancel, count.get(), "Count should not increase after cancel");
    }

    @Test
    @DisplayName("delay should create non-blocking delay")
    void delay_createsNonBlockingDelay() throws InterruptedException {
        long start = System.currentTimeMillis();

        var future = this.executor.delay(Duration.ofMillis(200));

        long afterCall = System.currentTimeMillis();
        assertTrue(afterCall - start < 50, "delay() should return immediately");

        CountDownLatch latch = new CountDownLatch(1);
        future.thenRun(latch::countDown);

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 200 && elapsed < 500, "Should complete after ~200ms, took: " + elapsed);
    }

    @Test
    @DisplayName("shutdown should complete pending tasks")
    void shutdown_completesPendingTasks() throws InterruptedException {
        AtomicInteger completed = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            this.executor.submit(() -> {
                try {
                    Thread.sleep(100);
                    completed.incrementAndGet();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        boolean success = this.executor.shutdown(Duration.ofSeconds(5));

        assertTrue(success);
        assertEquals(5, completed.get());
        assertTrue(this.executor.isShutdown());
    }

    @Test
    @DisplayName("shutdownNow should stop immediately")
    void shutdownNow_stopsImmediately() {
        for (int i = 0; i < 10; i++) {
            this.executor.submit(() -> {
                try {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        this.executor.shutdownNow();

        assertTrue(this.executor.isShutdown());
    }

    @Test
    @DisplayName("submit after shutdown should not execute")
    void submit_afterShutdown_doesNotExecute() throws InterruptedException {
        this.executor.shutdown(Duration.ofSeconds(1));

        AtomicBoolean executed = new AtomicBoolean(false);
        this.executor.submit(() -> executed.set(true));

        Thread.sleep(200);

        assertFalse(executed.get());
    }

    @Test
    @DisplayName("submit should handle exceptions without crashing")
    void submit_handlesExceptions() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        this.executor.submit(() -> {
            latch.countDown();
            throw new RuntimeException("Test exception");
        });

        this.executor.submit(latch::countDown);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Second task should still execute");
    }
}
