package de.jsilbereisen.perfumator.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for checking different constraints.
 */
public final class ConstraintsUtil {

    private ConstraintsUtil() { }

    /**
     * Returns {@code true} if any of the given objects is {@code null}.
     */
    public static boolean anyNull(Object... nullables) {
        return Arrays.stream(nullables).anyMatch(Objects::isNull);
    }
}
