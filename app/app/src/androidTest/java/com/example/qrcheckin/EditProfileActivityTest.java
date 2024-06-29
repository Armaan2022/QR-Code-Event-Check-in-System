package com.example.qrcheckin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.Activity;
import android.app.Instrumentation;

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
public class EditProfileActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> scenario = new
            ActivityScenarioRule<ProfileActivity>(ProfileActivity.class);


    @Before
    public void setUp() {
        Intents.release();
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void editProfileTest() {

        Espresso.onView(ViewMatchers.withId(R.id.button_edit_events)).perform(ViewActions.click());

        //As an attendee, I want to edit my profile.
        String Name = "New Example";
        String Phone = "1234567890";
        String Email = "new_example@ualberta.ca";

        Espresso.onView(ViewMatchers.withId(R.id.editUserNameText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.edituserPhoneText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.edituserEmailText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.editUserNameText)).perform(ViewActions.typeText(Name), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.edituserPhoneText)).perform(ViewActions.typeText(Phone), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.edituserEmailText)).perform(ViewActions.typeText(Email), ViewActions.closeSoftKeyboard());

        Espresso.closeSoftKeyboard();

        // Check if the intent is created
        // Developer.Android, 2024, Source: https://developer.android.com/training/testing/espresso/intents#java
        intending(hasComponent(HomepageActivity.class.getName())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
        Espresso.onView(withId(R.id.contAddProfileButton)).perform(click());
        intended(IntentMatchers.hasComponent(HomepageActivity.class.getName()));
    }

}