package com.eternalcode.commons.scheduler.loom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link LoomFuture}.
 */
@Timeout(10)
class LoomFutureTest {

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
    @DisplayName("thenApply should transform value")
    void thenApply_transformsValue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> 42)
            .thenApply(n -> "Number: " + n)
            .thenApply(String::toUpperCase)
            .thenAccept(s -> {
                result.set(s);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("NUMBER: 42", result.get());
    }

    @Test
    @DisplayName("thenAccept should consume value")
    void thenAccept_consumesValue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> "test value")
            .thenAccept(value -> {
                result.set(value);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("test value", result.get());
    }

    @Test
    @DisplayName("thenRun should execute action")
    void thenRun_executesAction() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ran = new AtomicBoolean(false);

        this.scheduler.supplyAsync(() -> "ignored")
            .thenRun(() -> {
                ran.set(true);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(ran.get());
    }

    @Test
    @DisplayName("thenCompose should chain futures")
    void thenCompose_chainsFutures() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> "Hello")
            .thenCompose(s -> this.scheduler.supplyAsync(() -> s + " World"))
            .thenAccept(s -> {
                result.set(s);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("Hello World", result.get());
    }

    @Test
    @DisplayName("thenApplySync should transform on main thread")
    void thenApplySync_transformsOnMainThread() throws InterruptedException {
        CountDownLatch asyncLatch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                asyncLatch.countDown();
                return 100;
            })
            .thenApplySync(n -> "Value: " + n)
            .thenAcceptSync(s -> result.set(s));

        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        // Should have 2 queued tasks (thenApplySync and thenAcceptSync)
        assertTrue(this.dispatcher.getPendingCount() >= 1);

        this.dispatcher.runPending();
        Thread.sleep(50);
        this.dispatcher.runPending();

        assertEquals("Value: 100", result.get());
    }

    @Test
    @DisplayName("thenAcceptSync should execute on main thread")
    void thenAcceptSync_executesOnMainThread() throws InterruptedException {
        CountDownLatch asyncLatch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        this.scheduler.supplyAsync(() -> {
                asyncLatch.countDown();
                return "data";
            })
            .thenAcceptSync(data -> executed.set(true));

        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        assertFalse(executed.get(), "Should not have executed yet");

        this.dispatcher.runPending();

        assertTrue(executed.get(), "Should have executed after drain");
    }

    @Test
    @DisplayName("thenRunSync should execute action on main thread")
    void thenRunSync_executesOnMainThread() throws InterruptedException {
        CountDownLatch asyncLatch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        this.scheduler.supplyAsync(() -> {
                asyncLatch.countDown();
                return "ignored";
            })
            .thenRunSync(() -> executed.set(true));

        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        this.dispatcher.runPending();

        assertTrue(executed.get());
    }

    @Test
    @DisplayName("exceptionally should handle errors")
    void exceptionally_handlesErrors() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorMessage = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                throw new RuntimeException("Test error");
            })
            .exceptionally(error -> {
                errorMessage.set(error.getMessage());
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(errorMessage.get().contains("Test error"));
    }

    @Test
    @DisplayName("exceptionallyRecover should provide fallback")
    void exceptionallyRecover_providesFallback() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                throw new RuntimeException("Error");
            })
            .exceptionallyRecover(error -> "recovered")
            .thenAccept(value -> {
                result.set((String) value);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("recovered", result.get());
    }

    @Test
    @DisplayName("whenComplete should handle both success and failure")
    void whenComplete_handlesBoth() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> "success")
            .whenComplete((value, ex) -> {
                result.set(value);
                error.set(ex);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("success", result.get());
        assertNull(error.get());
    }

    @Test
    @DisplayName("whenCompleteSync should execute on main thread")
    void whenCompleteSync_executesOnMainThread() throws InterruptedException {
        CountDownLatch asyncLatch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                asyncLatch.countDown();
                return "value";
            })
            .whenCompleteSync((value, error) -> result.set(value));

        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        this.dispatcher.runPending();

        assertEquals("value", result.get());
    }

    @Test
    @DisplayName("timeout should fail on timeout")
    void timeout_failsOnTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean timedOut = new AtomicBoolean(false);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "too late";
            })
            .timeout(Duration.ofMillis(100))
            .exceptionally(error -> {
                errorRef.set(error);
                // TimeoutException can be the error itself or its cause
                boolean isTimeout = error instanceof TimeoutException
                    || (error.getCause() != null && error.getCause() instanceof TimeoutException);
                timedOut.set(isTimeout);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(timedOut.get(), "Should have timed out, but error was: " + errorRef.get());
    }

    @Test
    @DisplayName("timeoutFallback should provide fallback on timeout")
    void timeoutFallback_providesFallbackOnTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "too late";
            })
            .timeoutFallback(Duration.ofMillis(100), "fallback")
            .thenAccept(value -> {
                result.set(value);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("fallback", result.get());
    }

    @Test
    @DisplayName("cancel should mark future as cancelled")
    void cancel_marksFutureAsCancelled() {
        LoomFuture<String> future = this.scheduler.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "never";
        });

        future.cancel();

        assertTrue(future.isCancelled());
    }

    @Test
    @DisplayName("asFuture should return underlying future")
    void asFuture_returnsUnderlyingFuture() {
        LoomFuture<String> future = this.scheduler.supplyAsync(() -> "test");

        assertNotNull(future.asFuture());
    }

    @Test
    @DisplayName("isDone should reflect completion state")
    void isDone_reflectsCompletionState() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        LoomFuture<String> future = this.scheduler.supplyAsync(() -> {
            latch.countDown();
            return "done";
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        assertTrue(future.isDone());
    }

    @Test
    @DisplayName("isAsync should return true")
    void isAsync_returnsTrue() {
        LoomFuture<String> future = this.scheduler.supplyAsync(() -> "test");

        assertTrue(future.async());
    }

    @Test
    @DisplayName("isRepeating should return false")
    void isRepeating_returnsFalse() {
        LoomFuture<String> future = this.scheduler.supplyAsync(() -> "test");

        assertFalse(future.repeating());
    }

    @Test
    @DisplayName("long chain should work correctly")
    void longChain_worksCorrectly() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> 1)
            .thenApply(n -> n + 1)
            .thenApply(n -> n * 2)
            .thenApply(n -> n + 10)
            .thenApply(n -> n * 3)
            .thenAccept(n -> {
                result.set(n);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(42, result.get()); // ((1+1)*2+10)*3 = 42
    }

    @Test
    @DisplayName("mixed async and sync chain should work")
    void mixedChain_works() throws InterruptedException {
        CountDownLatch asyncLatch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();

        this.scheduler.supplyAsync(() -> {
                asyncLatch.countDown();
                return 10;
            })
            .thenApply(n -> n * 2) // async
            .thenApplySync(n -> n + 5) // sync (queued)
            .thenApply(n -> "Result: " + n) // async
            .thenAcceptSync(s -> result.set(s)); // sync (queued)

        assertTrue(asyncLatch.await(5, TimeUnit.SECONDS));
        Thread.sleep(200);

        // Drain sync tasks
        while (this.dispatcher.hasPending()) {
            this.dispatcher.runPending();
            Thread.sleep(50);
        }

        assertEquals("Result: 25", result.get());
    }
}
