package com.example.qrcheckin;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class EventTest {

    private Event eventWithCapacity;
    private Event eventWithoutCapacity;
    private Profile testProfile;

    @Before
    public void setUp() {
        // Assuming Profile has a simple constructor (not shown here for brevity)
        testProfile = new Profile("TestProfileID", "Test Name", "test@example.com","nb");

        eventWithCapacity = new Event("EventName", "Description", "Start", "End", "Location", 2, "QRCode", "PromoQR", "Poster", "OrganizerID");
        ArrayList<Profile> signedUpAttendees = new ArrayList<>();
        // Simulating adding an attendee to mimic a partially filled event.
        signedUpAttendees.add(testProfile);
        eventWithCapacity.setSignedUpAttendees(signedUpAttendees);

        eventWithoutCapacity = new Event("EventName", "Description", "Start", "End", "Location", "QRCode", "Poster", "OrganizerID");
        // For eventWithoutCapacity, no need to set SignedUpAttendees as it's initially empty and has no capacity limit.
    }

    @Test
    public void testReachedCapacity_NotReached_ReturnsFalse() {
        assertFalse(eventWithCapacity.reachedCapacity());
    }
    /*
    @Test
    public void testReachedCapacity_Reached_ReturnsTrue() throws Exception {
        eventWithCapacity.signUpAttendee(new Profile("AnotherProfileID", "Another Test Name", "test2@example.com","nb"));
        assertTrue(eventWithCapacity.reachedCapacity());
    }

    @Test(expected = Exception.class)
    public void testSignUpAttendee_AtCapacity_ThrowsException() throws Exception {
        // Adding the second attendee to reach capacity.
        eventWithCapacity.signUpAttendee(new Profile("AnotherProfileID", "Another Test Name", "test2@example.com","nb"));
        // Attempting to add another should throw an exception.
        eventWithCapacity.signUpAttendee(new Profile("ThirdProfileID", "Third Test Name", "test3@example.com","nb"));
    }

    @Test
    public void testSignUpAttendee_NotAtCapacity_Succeeds() throws Exception {
        // This should succeed without throwing an exception.
        eventWithCapacity.signUpAttendee(new Profile("AnotherProfileID", "Another Test Name", "test2@example.com","nb"));
        assertEquals(2, eventWithCapacity.getSignedUpAttendees().size());
    }
    */
    // Additional tests can be written for getters and setters as needed.
}

