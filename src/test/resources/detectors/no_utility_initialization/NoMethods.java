package de.jsilbereisen.perfumator.test;

/**
 * Here, <b>no</b> instance of the "No Utility initialization" Perfume should be detected,
 * as the class does not declare any method, so it is not really worth rewarding; There is no visible
 * reason why the constructor is private.
 */
public class NoMethods {

    private NoMethods() { }
}