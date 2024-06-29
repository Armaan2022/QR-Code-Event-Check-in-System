package com.example.qrcheckin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Base64;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;



public class HelpersTest {

    @Test
    public void testCreateDocID() {
        String docID = Helpers.createDocID("field1", "field2", "field3");
        assertNotNull(docID);
        assertNotEquals("", docID); // Ensure it's not an empty string
        // Further checks could involve verifying the length of the output or pattern,
        // depending on the expected hash string format.
    }
    @Test
    public void testReverseString() {
        assertEquals("cba", Helpers.reverseString("abc"));
        assertEquals("321", Helpers.reverseString("123"));
        assertEquals("", Helpers.reverseString("")); // Edge case: empty string
        assertEquals("dcba", Helpers.reverseString("abcd")); // Even length string
    }
}


