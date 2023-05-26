package de.jsilbereisen.perfumator.model;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * {@link Comparator} to sort {@link Detectable}s by their names.
 */
public class DetectableComparator implements Comparator<Detectable> {

    /**
     * Compares two {@link Detectable}s. Uses {@link Detectable#compareTo} in the process.
     */
    @Override
    public int compare(@Nullable Detectable o1, @Nullable Detectable o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1;
            }
        }

        return o1.compareTo(o2);
    }
}
