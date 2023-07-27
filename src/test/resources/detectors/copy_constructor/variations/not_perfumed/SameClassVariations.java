package de.jsilbereisen.test;

public class SameClassVariations {

    private int x;

    final int y;

    // No parameter
    public SameClassVariations() {
        this.x = other.x;
        this.y = other.y;
    }

    // Too many parameters
    public SameClassVariations(SameClassVariations other, int arg) {
        this.x = other.x;
        this.y = other.y;
    }

    // Unrecognized copy methods/fields
    public SameClassVariations(SameClassVariations other) {
        this.x = unrecognizedCopy(other.x);
        this.x = other.otherField;
        this.y = other.y.copyCopyCopy();
        this.y = other.y.copy(someArg);
    }
}