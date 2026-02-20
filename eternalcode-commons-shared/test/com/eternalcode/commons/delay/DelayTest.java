package com.eternalcode.commons.delay;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DelayTest {

    @Test
    void shouldExpireAfterDefaultDelay() {
        Delay<UUID> delay = Delay.withDefault(() -> Duration.ofMillis(500));
        UUID key = UUID.randomUUID();

        delay.markDelay(key);
        assertThat(delay.hasDelay(key)).isTrue();

        await()
            .pollDelay(250, MILLISECONDS)
            .atMost(500, MILLISECONDS)
            .until(() -> delay.hasDelay(key));

        await()
            .atMost(Duration.ofMillis(350)) // After previously await (600 ms - 900 ms)
            .until(() -> !delay.hasDelay(key));
    }

    @Test
    void shouldDoNotExpireBeforeCustomDelay() {
        Delay<UUID> delay = Delay.withDefault(() -> Duration.ofMillis(500));
        UUID key = UUID.randomUUID();

        delay.markDelay(key, Duration.ofMillis(1000));
        assertThat(delay.hasDelay(key)).isTrue();

        await()
            .pollDelay(500, MILLISECONDS)
            .atMost(1000, MILLISECONDS)
            .until(() -> delay.hasDelay(key));

        await()
            .atMost(600, MILLISECONDS) // After previously await (1100 ms - 1600 ms)
            .until(() -> !delay.hasDelay(key));
    }

    @Test
    void shouldUnmarkDelay() {
        Delay<UUID> delay = Delay.withDefault(() -> Duration.ofMillis(500));
        UUID key = UUID.randomUUID();

        delay.markDelay(key);
        assertThat(delay.hasDelay(key)).isTrue();

        delay.unmarkDelay(key);
        assertThat(delay.hasDelay(key)).isFalse();
    }

    @Test
    void shouldNotHaveDelayOnNonExistentKey() {
        Delay<UUID> delay = Delay.withDefault(() -> Duration.ofMillis(500));
        UUID key = UUID.randomUUID();

        assertThat(delay.hasDelay(key)).isFalse();
    }

    @Test
    void shouldReturnCorrectRemainingTime() {
        Delay<UUID> delay = Delay.withDefault(() -> Duration.ofMillis(500));
        UUID key = UUID.randomUUID();

        delay.markDelay(key, Duration.ofMillis(1000));

        // Immediately after marking, remaining time should be close to the full delay
        assertThat(delay.getRemaining(key))
            .isCloseTo(Duration.ofMillis(1000), Duration.ofMillis(150));

        // Wait for some time
        await()
            .pollDelay(400, MILLISECONDS)
            .atMost(550, MILLISECONDS)
            .untilAsserted(() -> {
                // After 400ms, remaining time should be less than the original
                assertThat(delay.getRemaining(key)).isLessThan(Duration.ofMillis(1000).minus(Duration.ofMillis(300)));
            });

        await()
            .atMost(Duration.ofMillis(1000).plus(Duration.ofMillis(150)))
            .until(() -> !delay.hasDelay(key));

        // After expiration, remaining time should be negative
        assertThat(delay.getRemaining(key)).isZero();
    }

    @Test
    void shouldHandleMultipleKeysIndependently() {
        Delay<UUID> delay = Delay.withDefault(() -> Duration.ofMillis(500));
        UUID shortTimeKey = UUID.randomUUID(); // 500ms
        UUID longTimeKey = UUID.randomUUID(); // 1000ms

        delay.markDelay(shortTimeKey);
        delay.markDelay(longTimeKey, Duration.ofMillis(1000));

        assertThat(delay.hasDelay(shortTimeKey)).isTrue();
        assertThat(delay.hasDelay(longTimeKey)).isTrue();

        // Wait for the first key to expire
        await()
            .atMost(Duration.ofMillis(500).plus(Duration.ofMillis(150)))
            .until(() -> !delay.hasDelay(shortTimeKey));

        // After first key expires, second should still be active
        assertThat(delay.hasDelay(shortTimeKey)).isFalse();
        assertThat(delay.hasDelay(longTimeKey)).isTrue();

        // Wait for the second key to expire
        await()
            .atMost(Duration.ofMillis(1000))
            .until(() -> !delay.hasDelay(longTimeKey));

        assertThat(delay.hasDelay(longTimeKey)).isFalse();
    }

    @Test
    void testExpireAfterCreate_withOverflow_shouldReturnMaxValue() {
        Delay.InstantExpiry<String> expiry = new Delay.InstantExpiry<>();
        Instant farFuture = Instant.now().plus(Duration.ofDays(1000000000));

        long result = expiry.expireAfterCreate("key", farFuture, 0);

        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    void testExpireAfterCreate_withOverflow_shouldReturnMinValue() {
        Delay.InstantExpiry<String> expiry = new Delay.InstantExpiry<>();
        Instant farPast = Instant.now().minus(Duration.ofDays(1000000000));

        long result = expiry.expireAfterCreate("key", farPast, 0);

        assertEquals(Long.MIN_VALUE, result);
    }

    @Test
    void testSuperLargeDelay() {
        Delay<UUID> delay = Delay.withDefault(() -> Duration.ofDays(1000000000));
        UUID key = UUID.randomUUID();

        delay.markDelay(key);
        assertThat(delay.hasDelay(key)).isTrue();

        await()
            .atMost(Duration.ofSeconds(1))
            .until(() -> delay.hasDelay(key));

        // Even after waiting, the delay should still be active due to the large duration
        assertThat(delay.hasDelay(key)).isTrue();
    }
}
