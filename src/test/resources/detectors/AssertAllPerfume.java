package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertAll;

public class AssertAllPerfume {
    
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