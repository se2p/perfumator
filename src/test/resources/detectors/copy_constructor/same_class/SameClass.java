package de.jsilbereisen.test;

import java.nio.file.Path;

public class SameClass {

    int x;

    int y;

    String str;

    // Should not be considered, is static
    static float f;

    // Should not be considered, is final + initialized
    public final double d = 1.0;

    Path p;

    public SameClass() {
        // Not a copy constructor
    }

    public SameClass(SameClass o) {
        this.x = o.x;
        this.y = o.y;
        this.str = o.str;
        this.p = o.p;
    }
}