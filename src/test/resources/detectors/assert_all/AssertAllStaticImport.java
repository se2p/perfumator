package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;

public class AssertAllStaticImport {
    
    @Test
    void testWithAssertAll() {
        String i = "1";
        int j = 2;
        int k = 3;
        
        assertAll(
            "grouped type assertions",
            () -> assertThat(i instanceof Integer),
            () -> assertThat(j instanceof Integer),
            () -> assertThat(k instanceof Integer)
        );
    }
}
