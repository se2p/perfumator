package de.jsilbereisen.perfumator.test;

/**
 * Here, an instance of the "No Utility initialization" Perfume should be detected,
 * as the class only declares static methods and explicitely prevents instanciation
 * by declaring a single private constructor.
 */
public class NoUtilityInitializationPerfume {

    private NoUtilityInitializationPerfume() { }

    public static int method1() {
        return 1;
    }

    public static void method2() { }

    private static void internalHelperMethod() { }
}