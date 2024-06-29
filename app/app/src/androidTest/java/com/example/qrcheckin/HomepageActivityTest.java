package com.example.qrcheckin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomepageActivityTest {
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
    @Before
    public void setup() {
        ActivityScenario.launch(HomepageActivity.class);
    }
    @Rule
    public ActivityScenarioRule<HomepageActivity> scenario = new
            ActivityScenarioRule<HomepageActivity>(HomepageActivity.class);

    @Test
    public void SignedUpEventButtonTest(){
        onView(withId(R.id.button_signed_up_events)).perform(click());
        onView(withId(R.id.button_check_in)).check(matches(isDisplayed()));
    }
@Test
    public void ProfileButtonTest(){
        onView(withId(R.id.profile_image_button)).perform(click());
        onView(withId(R.id.button_back)).check(matches(isDisplayed()));
    }

    @Test
    public void OrganizeEventsButtonTest(){
        onView(withId(R.id.button_organize_events)).perform(click());
        onView(withId(R.id.button_create_event)).check(matches(isDisplayed()));
    }

    @Test
    public void US02_09_01() {
        //As an attendee, I want to know what events I signed up for currently and in and in the future.
        onView(withId(R.id.button_signed_up_events)).perform(click());
        onView(withId(R.id.button_home)).perform(click());
    }
    @Test
    public void US_02_01_01() {

        //As an attendee, I want to quickly check into an event by scanning the provided QR code.
        onView(withId(R.id.button_check_in)).perform(click());
    }

    @Test
    public void US02_07_01() {
        //As an attendee, I want to sign up to attend an event from the event details
        // (as in I promise to go).
        onView(withId(R.id.event_list)).perform(click());
        onView(ViewMatchers.isRoot()).perform(ViewActions.swipeUp());
        onView(withId(R.id.signUpButton)).perform((click()));

    }
    @Test
    public void US02_01_01() {

        //As an attendee, I want to quickly check into an event by scanning the provided QR code.
        onView(withId(R.id.button_check_in)).perform(click());


    }
    @Test
    public void Homebuttontest(){
        onView(withId(R.id.button_organize_events)).perform(click());
        intended(hasComponent(HomepageOrganizer.class.getName()));
    }
}

