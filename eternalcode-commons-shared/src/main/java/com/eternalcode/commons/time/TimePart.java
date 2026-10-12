package com.eternalcode.commons.time;

import java.math.BigInteger;
import java.time.temporal.ChronoUnit;

public record TimePart(
    BigInteger count,
    String name,
    ChronoUnit unit
) {
}
