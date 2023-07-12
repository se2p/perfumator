package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

class PackagePrivateTestsMethodPublic {

    @Test
    void someTest() { }

    @Test
    void anotherTest() { }

    // PUBLIC
    @ParameterizedTest
    public void someParameterizedTest() { }

    void helperMethod() { }
}