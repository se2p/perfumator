package de.jsilbereisen.perfumator.model;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Implementation of {@link Comparator} for {@link DetectedInstance}s.
 *
 * @param <T> The (sub-)type of {@link Detectable} that is held by the {@link DetectedInstance}s to compare.
 */
public class DetectedInstanceComparator<T extends Detectable> implements Comparator<DetectedInstance<T>> {

    /**
     * Compares two {@link DetectedInstance}s. If the comparison is non-trivial (both are non-null),
     * calls {@link DetectedInstance#compareTo}.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return the comparison result, semantics are as describes in {@link Comparator}.
     */
    @Override
    public int compare(@Nullable DetectedInstance<T> o1, @Nullable DetectedInstance<T> o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        } else if (o2 == null) {
            return 1;
        }

        return o1.compareTo(o2);
    }
}
