package com.example.qrcheckin;

import android.os.Bundle;

/**
 * A utility class for processing and validating data passed via the intent.
 */
public class IntentDataProcessor {
    public static boolean isValidIntentData(Bundle data) {
        if(data == null) return false;
        return data.containsKey("profileName") && data.getString("profileName") != null &&
                data.containsKey("profileEmail") && data.getString("profileEmail") != null &&
                data.containsKey("profilePhone") && data.getString("profilePhone") != null &&
                data.containsKey("profileUrl") && data.getString("profileUrl") != null &&
                data.containsKey("userID") && data.getString("userID") != null;
    }
}


