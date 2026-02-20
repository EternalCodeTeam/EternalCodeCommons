package com.eternalcode.commons.delay;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class Delay<T> {

    private final Cache<T, Instant> cache;
    private final Supplier<Duration> defaultDelay;

    private Delay(Supplier<Duration> defaultDelay) {
        if (defaultDelay == null) {
            throw new IllegalArgumentException("defaultDelay cannot be null");
        }

        this.defaultDelay = defaultDelay;
        this.cache = Caffeine.newBuilder()
            .expireAfter(new InstantExpiry<T>())
            .build();
    }

    public static <T> Delay<T> withDefault(Supplier<Duration> defaultDelay) {
        return new Delay<>(defaultDelay);
    }

    public void markDelay(T key, Duration delay) {
        if (delay.isZero() || delay.isNegative()) {
            this.cache.invalidate(key);
            return;
        }

        this.cache.put(key, Instant.now().plus(delay));
    }

    public void markDelay(T key) {
        this.markDelay(key, this.defaultDelay.get());
    }

    public void unmarkDelay(T key) {
        this.cache.invalidate(key);
    }

    public boolean hasDelay(T key) {
        Instant delayExpireMoment = this.getExpireAt(key);
        if (delayExpireMoment == null) {
            return false;
        }
        return Instant.now().isBefore(delayExpireMoment);
    }

    public Duration getRemaining(T key) {
        Instant expireAt = this.getExpireAt(key);
        if (expireAt == null) {
            return Duration.ZERO;
        }
        return Duration.between(Instant.now(), expireAt);
    }

    @Nullable
    public Instant getExpireAt(T key) {
        return this.cache.getIfPresent(key);
    }

    public static class InstantExpiry<T> implements Expiry<T, Instant> {

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
}
