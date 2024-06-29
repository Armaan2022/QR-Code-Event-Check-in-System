package com.example.qrcheckin;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileTest {

    private Profile profile;

    @Before
    public void setUp() {
        profile = new Profile("John Doe", "1234567890", "johndoe@example.com", "http://johndoe.com");
        profile.setUserID("uniqueUserID");
        // Simulate adding checked-in events if needed
    }

    @Test
    public void testGettersAndSetters() {
        // Test user ID
        assertEquals("uniqueUserID", profile.getUserID());

        // Test user name
        assertEquals("John Doe", profile.getUserName());
        profile.setUserName("Jane Doe");
        assertEquals("Jane Doe", profile.getUserName());

        // Test phone
        assertEquals("1234567890", profile.getPhone());
        profile.setPhone("0987654321");
        assertEquals("0987654321", profile.getPhone());

        // Test email
        assertEquals("johndoe@example.com", profile.getEmail());
        profile.setEmail("janedoe@example.com");
        assertEquals("janedoe@example.com", profile.getEmail());

        // Test homepage
        assertEquals("http://johndoe.com", profile.getHomepage());
        profile.setHomepage("http://janedoe.com");
        assertEquals("http://janedoe.com", profile.getHomepage());
    }
}

