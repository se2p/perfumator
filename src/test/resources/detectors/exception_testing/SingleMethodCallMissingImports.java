package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;

/**
 * All calls here are perfumed, but none should be detected, because the import is missing.
 * This is an optimization to avoid unnecessary walking of ASTs, searching for method calls that would not
 * even compile.
 */
public class SingleMethodCallMissingImports {

    @Test
    void singleMethodCallJUnit() {
        Object test = new Object();
        Assert.assertThrows(IllegalStateException.class, () -> test.toString());
    }

    @Test
    void singleMethodCallAssertJ() {
        Object test = new Object();
        assertThatThrownBy(() -> test.toString()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void singleMethodCallJUnit() {
        Object test = new Object();
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> test.toString());
    }

    @Test
    void singleMethodCallJUnitTryCatchIdiom() {
        Object test = new Object();
        try {
            test.toString();
            Assert.fail("Should have thrown IllegalStateException.");
        } catch (IllegalStateException e) {
            // Ignore here
        }
    }
}