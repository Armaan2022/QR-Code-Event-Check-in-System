package com.example.qrcheckin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignedUpEventActivityTest {
    @Before
    public void setUp() {
        // Initialize Espresso Intents before each test
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release Espresso Intents after each test
        Intents.release();
    }
    @Rule
    public ActivityScenarioRule<SignedUpEventActivity> scenario = new
            ActivityScenarioRule<SignedUpEventActivity>(SignedUpEventActivity.class);

    @Test
    public void opentest() {
        onView(withId(R.id.button_home)).check(matches(isDisplayed()));

    }


}