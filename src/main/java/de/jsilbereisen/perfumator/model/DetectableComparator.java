package de.jsilbereisen.perfumator.model;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * {@link Comparator} to sort {@link Detectable}s by their names.
 */
public class DetectableComparator<T extends Detectable> implements Comparator<T> {

    /**
     * Compares two {@link Detectable}s. Uses {@link Detectable#compareTo} in the process.
     */
    @Override
    public int compare(@Nullable T o1, @Nullable T o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        }

        return o1.compareTo(o2);
    }
}
