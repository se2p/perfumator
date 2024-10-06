package de.jsilbereisen.test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterizedTests {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ParameterizedTest {
    }
    
    @ParameterizedTest
    void paramterized() {
        // no perfume
    }

    @org.junit.jupiter.params.ParameterizedTest
    @ValueSource(ints = {1, 2})
    void paramterized(int i) {
        assertThat(i).isInstanceOf(Integer.class);
    }
}
