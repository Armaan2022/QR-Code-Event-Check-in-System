package com.example.qrcheckin;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests for the Announcement class.
 */
public class AnnouncementTest {

    private Announcement announcement;

    @Before
    public void setUp() {
        // Initialize your class with a known value before each test
        announcement = new Announcement("Initial Announcement");
    }

    @Test
    public void testGetAnnouncement_ReturnsCorrectAnnouncement() {
        // Test that the getter returns the correct initial value
        assertEquals("Initial Announcement", announcement.getAnnouncement());
    }

    @Test
    public void testSetAnnouncement_UpdatesAnnouncementCorrectly() {
        // Test that the setter correctly updates the announcement
        announcement.setAnnouncement("Updated Announcement");
        assertEquals("Updated Announcement", announcement.getAnnouncement());
    }
}
