package com.example.qrcheckin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.activity.result.ActivityResult;
import androidx.test.espresso.intent.Intents;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateProfileActivityTest {
    @Rule
    public ActivityScenarioRule<CreateProfileActivity> scenario = new
            ActivityScenarioRule<CreateProfileActivity>(CreateProfileActivity.class);

    @Test
    public void opentest() {
        onView(withId(R.id.profileImage)).check(matches(isDisplayed()));
    }
    @Test
    public void ADD_First_Profile_Test() {

        Intents.init();

        //As an attendee, I want to create my profile.
        String Name = "Example";
        String Phone = "1234567890";
        String Email = "example@ualberta.ca";

        Espresso.onView(ViewMatchers.withId(R.id.userNameEditText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.userPhoneEditText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.userEmailEditText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.userNameEditText)).perform(ViewActions.typeText(Name), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.userPhoneEditText)).perform(ViewActions.typeText(Phone), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.userEmailEditText)).perform(ViewActions.typeText(Email), ViewActions.closeSoftKeyboard());

        // Check if the intent is created
        // Developer.Android, 2024, Source: https://developer.android.com/training/testing/espresso/intents#java
        intending(hasComponent(HomepageActivity.class.getName())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
        Espresso.onView(withId(R.id.continueAddProfileButton)).perform(click());
        intended(IntentMatchers.hasComponent(HomepageActivity.class.getName()));
    }
}