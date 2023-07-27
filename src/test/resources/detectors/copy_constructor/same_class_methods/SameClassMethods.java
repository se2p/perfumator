package de.jsilbereisen.test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

public class SameClassMethods {

    Cloneable x;

    Copyable y;

    String str;

    OtherObject z;

    List<String> myList;

    List<String> myOtherList = new ArrayList<>();

    // Should not be considered, is static
    static float f;

    // Should not be considered, is final + initialized
    public final double d = 1.0;

    Path p;

    public SameClassMethods() {
        // Not a copy constructor
    }

    public SameClassMethods(SameClassMethods o) {
        this.x = o.x.clone();
        this.y = o.y.copy();
        this.z = new OtherObject(o.z); // Usage of copy-constructor for the property
        this.str = createCopy(o.str);
        this.p = o.p;
        myList = new ArrayList<>(o.myList);
        myOtherList = UtilClass.copy(o.myOtherList, someFlagOrAnything);
    }
}