package com.eternalcode.commons;

import com.eternalcode.commons.scheduler.Scheduler;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class RandomElementUtil {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private RandomElementUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> Optional<T> randomElement(Collection<T> collection) {
        if (collection.isEmpty()) {
            return Optional.empty();
        }

        return collection.stream()
            .skip(RANDOM.nextInt(collection.size()))
            .findFirst();
    }
}
