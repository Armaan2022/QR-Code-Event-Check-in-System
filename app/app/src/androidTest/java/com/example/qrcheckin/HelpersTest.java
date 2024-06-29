package com.example.qrcheckin;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static
        androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

public class HelpersTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);
    @Test
    public void testCreateDocID() {
        String docID = Helpers.createDocID("field1", "field2", "field3");
        assertNotNull(docID);
        assertNotEquals("", docID); // Ensure it's not an empty string
        // Further checks could involve verifying the length of the output or pattern,
        // depending on the expected hash string format.
    }

    @Test
    public void testBitmapToBase64AndBack() {
        ActivityScenario<MainActivity> scenario = activityRule.getScenario();

        scenario.onActivity(activity -> {
            // Create a Bitmap for testing
            Bitmap originalBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(originalBitmap);
            canvas.drawColor(Color.BLUE);

            // Convert bitmap to Base64 string
            String base64String = Helpers.bitmapToBase64(originalBitmap);
            assertNotNull(base64String);

            // Convert Base64 string back to bitmap
            Bitmap convertedBitmap = Helpers.base64ToBitmap(base64String);
            assertNotNull(convertedBitmap);

            // Check if the original and converted bitmaps have the same dimensions
            assertEquals(originalBitmap.getWidth(), convertedBitmap.getWidth());
            assertEquals(originalBitmap.getHeight(), convertedBitmap.getHeight());

            // Optionally, compare pixel by pixel if needed
            for (int y = 0; y < originalBitmap.getHeight(); y++) {
                for (int x = 0; x < originalBitmap.getWidth(); x++) {
                    assertEquals(originalBitmap.getPixel(x, y), convertedBitmap.getPixel(x, y));
                }
            }
        });
    }
}