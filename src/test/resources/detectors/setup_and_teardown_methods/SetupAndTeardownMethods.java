package de.jsilbereisen.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

public class SetupAndTeardownMethodPerfumes {
    
    @BeforeAll
    void setUpBeforeAll() {
        // setup once before all tests
    }
    
    @BeforeEach
    void setUpBeforeEach() {
        // setup before every test
    }
    
    void test() {
        int i = 1;
        assertThat(i == 1);
    }
    
    @AfterEach
    void tearDownAfterEach() {
        // teardown after every test
    }
    
    @AfterAll
    void tearDownAfterAll() {
        // teardown after all tests
    }
}