package de.jsilbereisen.perfumator.test;

/**
 * Here, <b>no</b> instance of the "No Utility initialization" Perfume should be detected,
 * as the class explicitly declares a public constructor, meaning it can be instanciated.
 */
public class ExplicitPublicConstructor {

    public ExplicitPublicConstructor(String someArg) { }

    private ExplicitPublicConstructor() { }

    public static void someMethod() { }
}