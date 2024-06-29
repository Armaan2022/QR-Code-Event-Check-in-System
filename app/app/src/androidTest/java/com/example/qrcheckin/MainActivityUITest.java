package com.example.qrcheckin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);


    @Test
    public void US02_02_03() {

    //As an attendee, I want to update information such as name, homepage,
        // and contact information on my profile.

        onView(withId(R.id.profile_image_button)).perform(click());
        onView(withId(R.id.button_edit_events)).perform(click());

        String Name = "Example";
        String Phone = "1234567890";
        String Email = "example@ualberta.ca";

        Espresso.onView(ViewMatchers.withId(R.id.editUserNameText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.edituserPhoneText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.edituserEmailText)).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.editUserNameText)).perform(ViewActions.typeText(Name), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.edituserPhoneText)).perform(ViewActions.typeText(Phone), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.edituserEmailText)).perform(ViewActions.typeText(Email), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.contAddProfileButton)).perform(click());
      //  onView(withId(R.id.button_back)).perform(click());
    }

    @Test
    public void US02_09_01() {
        //As an attendee, I want to know what events I signed up for currently and in and in the future.
        onView(withId(R.id.button_signed_up_events)).perform(click());
        onView(withId(R.id.button_home)).perform(click());
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
    public void ACCESS_ADMIN() {
        //As an ADMIN, I'm going to Admin Homepage and going back to Home

        onView(withId(R.id.profile_image_button)).perform(click());
        onView(withId(R.id.button_admin)).check(matches(isDisplayed()));

        onView(withId(R.id.button_admin)).perform(click());
        onView(withId(R.id.button_admin_profiles)).perform(click());
        onView(withId(R.id.button_admin_events)).check(matches(isDisplayed()));

        onView(withId(R.id.button_admin_events)).perform(click());
        onView(withId(R.id.button_admin_profiles)).check(matches(isDisplayed()));

        onView(withId(R.id.button_admin_images)).perform(click());
        onView(withId(R.id.button_view_qr_codes)).check(matches(isDisplayed()));

        onView(withId(R.id.button_view_qr_codes)).perform(click());
        onView(withId(R.id.button_backArrow)).perform(click());
        onView(withId(R.id.button_backArrow)).perform(click());
        onView(withId(R.id.button_home)).check(matches(isDisplayed()));

        onView(withId(R.id.button_home)).perform(click());
        onView(withId(R.id.switch_geolocation)).check(matches(isDisplayed()));
    }

}