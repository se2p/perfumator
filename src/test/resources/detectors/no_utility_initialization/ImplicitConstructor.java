package de.jsilbereisen.perfumator.test;

/**
 * Here, <b>no</b> instance of the "No Utility initialization" Perfume should be detected,
 * as the class does not declare any constructor, meaning the implicit constructor can be used to
 * create instances of this class.
 */
public class ImplicitConstructor {

    public static void someMethod() { }
}