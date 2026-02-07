package com.eternalcode.commons.delay;

import com.github.benmanes.caffeine.cache.Expiry;

import java.time.Duration;
import java.time.Instant;

public class InstantExpiry<T> implements Expiry<T, Instant> {

    private long timeToExpire(Instant expireTime, long currentTimeNanos) {
        Instant currentInstant = Instant.ofEpochSecond(0, currentTimeNanos);
        Duration toExpire = Duration.between(currentInstant, expireTime);

        if (toExpire.isNegative() || toExpire.isZero()) {
            return 0;
        }

        try {
            return toExpire.toNanos();
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public long expireAfterCreate(T key, Instant expireTime, long currentTime) {
        return timeToExpire(expireTime, currentTime);
    }

    @Override
    public long expireAfterUpdate(T key, Instant newExpireTime, long currentTime, long currentDuration) {
        return timeToExpire(newExpireTime, currentTime);
    }

    @Override
    public long expireAfterRead(T key, Instant value, long currentTime, long currentDuration) {
        return currentDuration;
    }

}
