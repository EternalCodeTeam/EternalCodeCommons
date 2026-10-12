package com.eternalcode.commons.time;

import java.util.List;

public record TimeResult(
    List<TimePart> parts,
    boolean isNegative
) {
}
