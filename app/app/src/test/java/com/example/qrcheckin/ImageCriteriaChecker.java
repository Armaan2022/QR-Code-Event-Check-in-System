package com.example.qrcheckin;

import java.util.HashMap;
/**
 * A utility class for checking different criteria against image data.
 */
public class ImageCriteriaChecker {
    public boolean isImageValid(HashMap<String, String> imageData) {
        // Example condition: Check if image data contains a non-null, non-empty URL
        String url = imageData.get("url");
        return url != null && !url.trim().isEmpty();
    }
}
