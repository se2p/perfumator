package de.jsilbereisen.perfumator.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Utility class for {@link String} related operations.
 */
public class StringUtil {

    /**
     * {@link Pattern}, compiled from the regular expression for an empty or whitespace only {@link String}.
     */
    public static final Pattern EMPTY_STRING = Pattern.compile("^\\s*$");

    private StringUtil() {
    }

    /**
     * Returns whether the given {@link String} is {@code null} or empty, in the sense
     * that it only consists of whitespaces.
     */
    public static boolean isEmpty(@Nullable String str) {
        return str == null || EMPTY_STRING.matcher(str).matches();
    }

    /**
     * Returns {@code true} if any of the given {@link String}s is {@code null} or
     * empty, in the sense that it only consists of whitespaces. If no arguments are given,
     * {@code false} is returned as there is no empty string.
     */
    public static boolean anyEmpty(@NotNull String... strings) {
        return Arrays.stream(strings).anyMatch(StringUtil::isEmpty);
    }

    /**
     * Returns {@code true} if any of the given {@link String}s is {@code null} or
     * empty, in the sense that it only consists of whitespaces.
     * If the given {@link Collection} is empty, {@code false} is returned, as there is no empty string.
     */
    public static boolean anyEmpty(@NotNull Collection<String> strings) {
        return strings.stream().anyMatch(StringUtil::isEmpty);
    }

    /**
     * Joins all Strings in the given {@link Iterable} using the provided delimiter.
     * If there are no strings to join, the empty string with length 0 is returned.
     */
    public static @NotNull String joinStrings(@NotNull Iterable<String> strings, @NotNull String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = strings.iterator();

        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());

            if (iterator.hasNext()) {
                stringBuilder.append(delimiter);
            }
        }

        return stringBuilder.toString();
    }
}
