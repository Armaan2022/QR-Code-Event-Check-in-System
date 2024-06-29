package com.example.qrcheckin;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class QRScannerActivityTest {

    @Rule
    public ActivityScenarioRule<QRScannerActivity> activityScenarioRule = new ActivityScenarioRule<>(QRScannerActivity.class);

    @Test
    public void QRScannerRunning() throws UiObjectNotFoundException {
        // Assuming that the 'button_scan_qr' will trigger the permission dialog

        // Prepare UiDevice instance
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Wait for the permission dialog to appear
        UiObject allowButton = uiDevice.findObject(new UiSelector()
                .textMatches("(?i)allow|while using the app")
                .className("android.widget.Button"));

        if (allowButton.exists() && allowButton.isEnabled()) {
            allowButton.click();
        } else {
            throw new UiObjectNotFoundException("The Allow button on the permission dialog was not found.");
        }
    }
}

