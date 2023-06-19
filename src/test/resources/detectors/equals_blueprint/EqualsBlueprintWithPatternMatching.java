package de.jsilbereisen.test;

/**
 * Tests detection of the "Equals Blueprint" Perfume when pattern-matching is used
 * with the {@code instanceof} Operator.
 */
public class EqualsBlueprintWithPatternMatching {

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EqualsBlueprintWithPatternMatching casted)) {
            return false;
        }

        return casted != null;
    }
}