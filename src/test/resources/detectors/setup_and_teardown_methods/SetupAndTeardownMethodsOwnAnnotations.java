package de.jsilbereisen.test;

import static org.assertj.core.api.Assertions.assertThat;

public class SetupAndTeardownMethodPerfumes {

    @org.junit.jupiter.api.BeforeAll
    void setUpBeforeAll() {
        // setup once before all tests
    }

    @org.junit.jupiter.api.BeforeEach
    void setUpBeforeEach() {
        // setup before every test
    }

    void test() {
        int i = 1;
        assertThat(i == 1);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDownAfterEach() {
        // teardown after every test
    }

    @org.junit.jupiter.api.AfterAll
    void tearDownAfterAll() {
        // teardown after all tests
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BeforeAll {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BeforeEach {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AfterEach {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AfterAll {
    }

    @BeforeAll
    void setUpBeforeAll() {
        
    }

    @BeforeEach
    void setUpBeforeEach() {
        
    }

    void test() {
        int i = 1;
        assertThat(i == 1);
    }

    @AfterEach
    void tearDownAfterEach() {
        
    }

    @AfterAll
    void tearDownAfterAll() {
        
    }
}
