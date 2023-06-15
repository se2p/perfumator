package de.jsilbereisen.perfumator.test;

/**
 * Here, <b>no</b> instance of the "No Utility initialization" Perfume should be detected,
 * as the class declares at least one non-static method, meaning an instance is required to call that method.
 * In that case, declaring a private constructor does not really make sense.
 */
public class NonStaticMethod {

    private NonStaticMethod() { }

    public static void someStaticMethod() { }

    void nonStaticMethod() { }

    static void someOtherStaticMethod() { }
}