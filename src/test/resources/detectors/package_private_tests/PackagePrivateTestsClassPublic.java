package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

public class PackagePrivateTestsClassPublic {

    @Test
    void someTest() { }

    @Test
    void anotherTest() { }

    @ParameterizedTest
    void someParameterizedTest() { }

    private void helperMethod() { }
}