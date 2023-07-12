package de.jsilbereisen.test;

// None should be detected
class PackagePrivateTestsNoImports {

    @Test
    void someTest() { }

    @Test
    void anotherTest() { }

    @ParameterizedTest
    void someParameterizedTest() { }

    private void helperMethod() { }
}