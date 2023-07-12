package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert;
import static org.junit.jupiter.api.Assertions;

public class SingleMethodCallNotPerfumed {

    // Should not be detected: more than 1 method call
    @Test
    void singleMethodCallJUnit() {
        Object test = new Object();
        Assert.assertThrows(IllegalStateException.class, () -> test.getClass().toString());
    }

    // Should not be detected: more than 1 method call
    @Test
    void singleMethodCallAssertJ() {
        Object test = new Object();
        assertThatThrownBy(() -> test.getClass().toString()).isInstanceOf(IllegalStateException.class);
    }

    // Should not be detected: more than 1 method call
    @Test
    void singleMethodCallJUnit() {
        Object test = new Object();
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> test.getClass().toString());
    }

    // Should not be detected: arguments dont match
    @Test
    void singleMethodCallJUnit() {
        Object test = new Object();
        Assert.assertThrows(() -> test.toString());
    }

    // Should not be detected: arguments dont match
    @Test
    void singleMethodCallAssertJ() {
        Object test = new Object();
        assertThatThrownBy(1, () -> test.toString()).isInstanceOf(IllegalStateException.class);
    }

    // Should not be detected: arguments dont match
    @Test
    void singleMethodCallJUnit() {
        Object test = new Object();
        Assertions.assertThrowsExactly(1, () -> test.toString());
    }

    // Should not be detected: does not issue call via the Class name, but method not imported
    @Test
    void singleMethodCallJUnit() {
        Object test = new Object();
        assertThrowsExactly(IllegalStateException.class, () -> test.toString());
    }

    // Should not be detected, because the preferred Methods are called at least once!
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