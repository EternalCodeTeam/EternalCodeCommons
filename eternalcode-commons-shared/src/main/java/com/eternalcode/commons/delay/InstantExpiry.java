package com.eternalcode.commons.delay;

import com.github.benmanes.caffeine.cache.Expiry;

import java.time.Duration;
import java.time.Instant;

public class InstantExpiry<T> implements Expiry<T, Instant> {

    private long timeToExpire(Instant expireTime) {
        Duration toExpire = Duration.between(Instant.now(), expireTime);

        try {
            return toExpire.toNanos();
        } catch (ArithmeticException overflow) {
            return toExpire.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
    }

    @Override
    public long expireAfterCreate(T key, Instant expireTime, long currentTime) {
        return timeToExpire(expireTime);
    }

    @Override
    public long expireAfterUpdate(T key, Instant newExpireTime, long currentTime, long currentDuration) {
        return timeToExpire(newExpireTime);
    }

    @Override
    public long expireAfterRead(T key, Instant expireTime, long currentTime, long currentDuration) {
        return timeToExpire(expireTime);
    }

}
