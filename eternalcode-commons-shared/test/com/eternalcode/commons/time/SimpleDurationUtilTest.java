package com.eternalcode.commons.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class SimpleDurationUtilTest {

    @Test
    public void testFormatWithoutMillis() {
        Duration duration = Duration.ofMillis(500);
        String result = SimpleDurationUtil.format(duration, true);
        assertEquals("0s", result);

        duration = Duration.ofSeconds(30);
        result = SimpleDurationUtil.format(duration, true);
        assertEquals("30s", result);

        duration = Duration.ofMinutes(5);
        result = SimpleDurationUtil.format(duration, true);
        assertEquals("5m", result);

        duration = Duration.ofHours(2);
        result = SimpleDurationUtil.format(duration, true);
        assertEquals("2h", result);

        duration = Duration.ofDays(1);
        result = SimpleDurationUtil.format(duration, true);
        assertEquals("1d", result);

        duration = Duration.ofDays(14);
        result = SimpleDurationUtil.format(duration, true);
        assertEquals("2w", result);

        duration = Duration.ofDays(60);
        result = SimpleDurationUtil.format(duration, true);
        assertEquals("2mo", result);

        duration = Duration.ofDays(365 * 3);
        result = SimpleDurationUtil.format(duration, true);
        assertEquals("3y", result);
    }

    @Test
    public void testFormatWithMillis() {
        Duration duration = Duration.ofMillis(500);
        String result = SimpleDurationUtil.format(duration, false);
        assertEquals("500ms", result);

        duration = Duration.ofSeconds(30);
        result = SimpleDurationUtil.format(duration, false);
        assertEquals("30s", result);

        duration = Duration.ofMinutes(5);
        result = SimpleDurationUtil.format(duration, false);
        assertEquals("5m", result);

        duration = Duration.ofHours(2);
        result = SimpleDurationUtil.format(duration, false);
        assertEquals("2h", result);

        duration = Duration.ofDays(1);
        result = SimpleDurationUtil.format(duration, false);
        assertEquals("1d", result);

        duration = Duration.ofDays(14);
        result = SimpleDurationUtil.format(duration, false);
        assertEquals("2w", result);

        duration = Duration.ofDays(60);
        result = SimpleDurationUtil.format(duration, false);
        assertEquals("2mo", result);

        duration = Duration.ofDays(365 * 3);
        result = SimpleDurationUtil.format(duration, false);
        assertEquals("3y", result);
    }

    @Test
    public void testFormatDefault() {
        Duration duration = Duration.ofSeconds(610);
        String result = SimpleDurationUtil.format(duration);
        assertEquals("10m10s", result);

        duration = Duration.ofMillis(120);
        result = SimpleDurationUtil.format(duration);
        assertEquals("120ms", result);
    }
}
