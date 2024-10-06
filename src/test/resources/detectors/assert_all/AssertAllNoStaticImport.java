package de.jsilbereisen.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertAllNoStaticImport {
    
    @Test
    void testWithAssertAll() {
        String i = "1";
        int j = 2;
        int k = 3;

        Assertions.assertAll(
            "grouped type assertions",
            () -> assertThat(i instanceof Integer),
            () -> assertThat(j instanceof Integer),
            () -> assertThat(k instanceof Integer)
        );
    }
}