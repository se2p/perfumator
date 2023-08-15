package de.jsilbereisen.test;

import dep.*;

public class DependencyVariations extends Superclass implements SomeInterface {

    int field;

    public DependencyVariations(DependencyVariations other) {
        // No copy performed
    }

    // 2023-08-15 DO NOT SEE AS PERFUMED ANYMORE
    public DependencyVariations(Superclass other) {
        field = other.x; // Some other field is being copied, but we cant really verify its the right one
    }

    // 2023-08-15 DO NOT SEE AS PERFUMED ANYMORE
    public DependencyVariations(SomeInterface other) {
        copy(field, other.field); // Unrecognized form of copying
    }

    public DependencyVariations(NoSuperclass other) {
        this.field = other.field;
    }
}