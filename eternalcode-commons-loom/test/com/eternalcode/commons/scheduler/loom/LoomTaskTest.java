package com.eternalcode.commons.scheduler.loom;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link LoomTask} interface and implementations.
 */
@Timeout(10)
class LoomTaskTest {

    @Test
    @DisplayName("EMPTY task should be cancelled")
    void empty_isCancelled() {
        LoomTask task = LoomTask.EMPTY;

        assertTrue(task.isCancelled());
    }

    @Test
    @DisplayName("EMPTY task should be done")
    void empty_isDone() {
        LoomTask task = LoomTask.EMPTY;

        assertTrue(task.isDone());
    }

    @Test
    @DisplayName("EMPTY task should not be running")
    void empty_isNotRunning() {
        LoomTask task = LoomTask.EMPTY;

        assertFalse(task.isRunning());
    }

    @Test
    @DisplayName("EMPTY task should not be async")
    void empty_isNotAsync() {
        LoomTask task = LoomTask.EMPTY;

        assertFalse(task.async());
    }

    @Test
    @DisplayName("EMPTY task should not be repeating")
    void empty_isNotRepeating() {
        LoomTask task = LoomTask.EMPTY;

        assertFalse(task.repeating());
    }

    @Test
    @DisplayName("EMPTY task should return null future")
    void empty_returnsNullFuture() {
        LoomTask task = LoomTask.EMPTY;

        assertNull(task.asFuture());
    }

    @Test
    @DisplayName("EMPTY task cancel should not throw")
    void empty_cancelDoesNotThrow() {
        LoomTask task = LoomTask.EMPTY;

        assertDoesNotThrow(task::cancel);
    }

    @Test
    @DisplayName("custom implementation should work")
    void custom_implementation_works() {
        LoomTask task = new LoomTask() {
            @Override
            public void cancel() {
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isRunning() {
                return true;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public boolean async() {
                return true;
            }

            @Override
            public boolean repeating() {
                return true;
            }

            @Override
            public Future<?> asFuture() {
                return null;
            }
        };

        assertFalse(task.isCancelled());
        assertTrue(task.isRunning());
        assertFalse(task.isDone());
        assertTrue(task.async());
        assertTrue(task.repeating());
    }
}
