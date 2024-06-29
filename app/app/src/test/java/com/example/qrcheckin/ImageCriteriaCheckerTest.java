package com.example.qrcheckin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;

//test BrowsImagesActivity
/**
 * Utility class for validating different scenarios when dealing with the images
 */
public class ImageCriteriaCheckerTest {

    @Test
    public void isImageValid_withValidUrl_returnsTrue() {
        ImageCriteriaChecker checker = new ImageCriteriaChecker();
        HashMap<String, String> imageData = new HashMap<>();
        imageData.put("url", "http://example.com/image.png");

        assertTrue(checker.isImageValid(imageData));
    }

    @Test
    public void isImageValid_withInvalidUrl_returnsFalse() {
        ImageCriteriaChecker checker = new ImageCriteriaChecker();
        HashMap<String, String> imageData = new HashMap<>();
        imageData.put("url", ""); // Invalid URL

        assertFalse(checker.isImageValid(imageData));
    }
}

