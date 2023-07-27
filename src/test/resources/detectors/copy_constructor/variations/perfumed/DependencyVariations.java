package de.jsilbereisen.test;

import dep.*;

public class DependencyVariations extends Superclass implements SomeInterface {

    int field;

    public DependencyVariations(DependencyVariations other) {
        field = other.field;
    }

    // The superclass doesnt have the field, just for testing the correct resolution
    public DependencyVariations(Superclass other) {
        field = other.field;
    }

    // Interface obviously doesnt have the field aswell
    public DependencyVariations(SomeInterface other) {
        field = other.field;
    }
}