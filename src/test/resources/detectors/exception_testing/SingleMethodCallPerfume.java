package de.jsilbereisen.test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert;

public class SingleMethodCallPerfume {

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
}