package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;

import static org.junit.Assert;

public class SingleMethodCallTryCatchNotPerfumed {

    // More than 1 method call
    @Test
    void singleMethodCallJUnitTryCatchIdiom() {
        Object test = new Object();
        try {
            getClass().getSimpleName();
            Assert.fail("Should have thrown IllegalStateException.");
        } catch (IllegalStateException e) {
            // Ignore here
        }
    }

    // No catch
    @Test
    void singleMethodCallJUnitTryCatchIdiom() {
        Object test = new Object();
        try {
            getClass();
            Assert.fail("Should have thrown IllegalStateException.");
        } finally {
            // ignored
        }
    }

    // Doesnt manually fail the test
    @Test
    void singleMethodCallJUnitTryCatchIdiom() {
        Object test = new Object();
        try {
            getClass();
        } catch (IllegalStateException e) {
            // Ignore here
        }
    }

    // Checks for more than one exception at a time
    @Test
    void singleMethodCallJUnitTryCatchIdiom() {
        Object test = new Object();
        try {
            getClass();
            Assert.fail("Should have thrown IllegalStateException.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Ignore here
        }
    }
}