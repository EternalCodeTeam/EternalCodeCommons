package com.eternalcode.commons.time;

import java.time.Duration;

/**
 * Utility for converting Duration to Minecraft server ticks.
 *
 * @see <a href="https://minecraft.wiki/w/Tick">Tick - Minecraft Wiki</a>
 */
public final class DurationTickUtil {

    private static final int TICK_DURATION_MILLIS = 50;

    private DurationTickUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts Duration to Minecraft server ticks.
     * One tick equals 50 milliseconds (20 TPS).
     *
     * @param duration the duration to convert
     * @return number of ticks
     * @throws ArithmeticException if the result overflows an int
     */
    public static int durationToTicks(Duration duration) {
        return Math.toIntExact(duration.toMillis() / TICK_DURATION_MILLIS);
    }
}
