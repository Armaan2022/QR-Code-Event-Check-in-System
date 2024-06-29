package com.example.qrcheckin;

import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

public class CustomIdlingResource {

    private static final String RESOURCE = "GLOBAL";
    private static CountingIdlingResource countingIdlingResource =
            new CountingIdlingResource(RESOURCE);

    public static void increment() {
        countingIdlingResource.increment();
    }

    public static void decrement() {
        if (!countingIdlingResource.isIdleNow()) {
            countingIdlingResource.decrement();
        }
    }

    public static IdlingResource getIdlingResource() {
        return countingIdlingResource;
    }
}