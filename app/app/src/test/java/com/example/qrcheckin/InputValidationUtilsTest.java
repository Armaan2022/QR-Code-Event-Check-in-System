package com.example.qrcheckin;
/**
 Utility class For testing the createEvent validation test
 */

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InputValidationUtilsTest {

    @Test
    public void nameIsValid() {
        assertTrue(InputValidationUtils.isNameValid("John Doe"));
        assertFalse(InputValidationUtils.isNameValid(""));
        assertFalse(InputValidationUtils.isNameValid(null));
        assertFalse(InputValidationUtils.isNameValid(new String(new char[201]).replace('\0', 'a'))); // 201 characters
    }

    @Test
    public void phoneIsValid() {
        assertTrue(InputValidationUtils.isPhoneValid("1234567890"));
        assertFalse(InputValidationUtils.isPhoneValid("12345"));
        assertFalse(InputValidationUtils.isPhoneValid(null));
        assertFalse(InputValidationUtils.isPhoneValid("abcdefghij")); // Non-numeric
    }

    @Test
    public void emailIsValid() {
        assertTrue(InputValidationUtils.isEmailValid("user@example.com"));
        assertFalse(InputValidationUtils.isEmailValid("user@"));
        assertFalse(InputValidationUtils.isEmailValid(null));
        assertFalse(InputValidationUtils.isEmailValid("userexample.com"));
    }
}
