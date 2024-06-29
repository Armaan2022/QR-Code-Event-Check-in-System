package com.example.qrcheckin;

/**
 * Extract the logic of Creating event and test it. Validation test
 */
public class EventValidator {
    public static boolean isInputValid(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public static boolean isCapacityValid(String capacity) {
        try {
            int cap = Integer.parseInt(capacity);
            return cap > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
