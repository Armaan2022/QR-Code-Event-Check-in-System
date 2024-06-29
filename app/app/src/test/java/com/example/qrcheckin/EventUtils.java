package com.example.qrcheckin;

import android.os.Bundle;

/**
 * Utility class meant to unit test isAttendee method
 */
public class EventUtils {

    public static boolean isAttendee(Bundle bundle) {
        if (bundle != null) {
            String origin = bundle.getString("origin");
            return "attendee".equals(origin);
        }
        return false;
    }
}
