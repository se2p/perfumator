package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterizedTests {
    
    @Test
    void nonParameterized1() {
        int i = 1;
        assertThat(i).isInstanceOf(Integer.class);
    }
    
    @Test
    void nonParameterized2() {
        int i = 2;
        assertThat(i).isInstanceOf(Integer.class);
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2}) 
    void paramterized(int i) {
        assertThat(i).isInstanceOf(Integer.class);
    }
}