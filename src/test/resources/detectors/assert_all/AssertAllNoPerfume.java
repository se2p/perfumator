package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;

public class AssertAllNoStaticImport {
    
    @Test
    void test() {
        assertAll();
    }
    
    private void assertAll() {
        // imposter assertAll, should not be detected
    }
}
