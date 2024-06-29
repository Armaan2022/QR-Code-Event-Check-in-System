package com.example.qrcheckin;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventValidatorTest {

    @Test
    public void inputIsValid() {
        assertTrue(EventValidator.isInputValid("Event Name"));
        assertFalse(EventValidator.isInputValid(""));
        assertFalse(EventValidator.isInputValid("  "));
        assertFalse(EventValidator.isInputValid(null));
    }

    @Test
    public void capacityIsValid() {
        assertTrue(EventValidator.isCapacityValid("100"));
        assertFalse(EventValidator.isCapacityValid("0"));
        assertFalse(EventValidator.isCapacityValid("-1"));
        assertFalse(EventValidator.isCapacityValid("not a number"));
    }
}
