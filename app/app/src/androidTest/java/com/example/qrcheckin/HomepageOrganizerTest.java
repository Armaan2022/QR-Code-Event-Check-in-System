package com.example.qrcheckin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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

import org.checkerframework.checker.units.qual.A;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomepageOrganizerTest {
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
        ActivityScenario.launch(HomepageOrganizer.class);
    }
    @Rule
    public ActivityScenarioRule<HomepageOrganizer> scenario = new
            ActivityScenarioRule<HomepageOrganizer>(HomepageOrganizer.class);

    /*
    @Test
    public void Openingtest() {

        onView(withId(R.id.button_create_event)).check(matches(isDisplayed()));

    }*/
    @Test
    public void CreateEventButtonTest(){
        onView(withId(R.id.button_create_event)).perform(click());
        onView(withId(R.id.editPosterImageButton)).check(matches(isDisplayed()));
    }
    @Test
    public void BackButtonTest(){
        onView(withId(R.id.button_back_events)).perform(click());
        onView(withId(R.id.button_organize_events)).check(matches(isDisplayed()));
    }

    @Test
    public void US01_01_01() {
        // As an organizer, I want to create a new event and generate a unique QR code for attendee check-ins.
        onView(withId(R.id.button_create_event)).perform(click());

        String Name = "Example Event";
        String Description = "This is a test event";
        String Location = "Test Location";
        String attendeeCapacity = "12";

        Espresso.onView(ViewMatchers.withId(R.id.eventNameEditText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.eventDescriptionEditText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.eventLocationEditText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.attendeeCapacityEditText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.eventNameEditText)).perform(ViewActions.typeText(Name), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.eventDescriptionEditText)).perform(ViewActions.typeText(Description), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.eventLocationEditText)).perform(ViewActions.typeText(Location), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.attendeeCapacityEditText)).perform(ViewActions.typeText(attendeeCapacity), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.continueCreateEventButton)).perform(click());

        onView(withId(R.id.generateCheckinQRCodeButton)).perform(click());

        intending(hasComponent(HomepageOrganizer.class.getName())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
        onView(withId(R.id.confirmEventCreationButton)).perform(click());
        intended(IntentMatchers.hasComponent(HomepageOrganizer.class.getName()));
    }

}