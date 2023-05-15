package de.jsilbereisen.perfumator.util;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Utility class for {@link String} related operations.
 */
public final class StringUtil {

    /**
     * {@link Pattern}, compiled from the regular expression for an empty or whitespace only {@link String}.
     */
    public static final Pattern EMPTY_STRING = Pattern.compile("^\\s*$");

    private StringUtil() { }

    /**
     * Returns whether the given {@link String} is {@code null} or empty, in the sense
     * that it only consists of whitespaces.
     */
    public static boolean isEmpty(String str) {
        return str == null || EMPTY_STRING.matcher(str).matches();
    }

    /**
     * Returns {@code true} if any of the given {@link String}s is {@code null} or
     * empty, in the sense that it only consists of whitespaces.
     */
    public static boolean anyEmpty(String... strings) {
        return Arrays.stream(strings).anyMatch(StringUtil::isEmpty);
    }
}
