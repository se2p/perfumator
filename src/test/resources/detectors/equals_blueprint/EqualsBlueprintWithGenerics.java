package de.jsilbereisen.test;

/**
 * Tests detection of the "Equals blueprint" perfume with usage of generic types.
 */
public class EqualsBlueprintWithGenerics<T,  S> {

    // Should be detected
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof EqualsBlueprintWithGenerics<T, S> casted)) {
            return false;
        }

        return true;
    }

    class DontDetect<A, X> {
        // Should NOT be detected, incorrect generic cast
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            } else if (!(other instanceof DontDetect casted)) {
                return false;
            }

            return true;
        }
    }
}