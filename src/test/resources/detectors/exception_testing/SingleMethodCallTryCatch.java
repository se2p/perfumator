package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;

import static org.junit.Assert;

public class SingleMethodCallTryCatch {

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