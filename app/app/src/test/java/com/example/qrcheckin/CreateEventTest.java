package com.example.qrcheckin;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CreateEventTest {
    // OpenAI, 2024, ChatGPT, How to add mock event details

    @Test
    public void testEventConstructorAndGetters() {
        // Arrange
        String eventName = "Test Event";
        String description = "This is a test event.";
        String start = "2021-12-01 10:00";
        String end = "2021-12-01 12:00";
        String location = "Test Location";
        String qrCode = "TestQRCode";
        String poster = "TestPosterUrl";
        String organizerID = "TestOrganizerID";

        // Act
        Event event = new Event(eventName, description, start, end, location, qrCode, poster, organizerID);

        // Assert
        assertEquals("Event name should match", eventName, event.getName());
        assertEquals("Description should match", description, event.getDescription());
        assertEquals("Start time should match", start, event.getStartTime());
        assertEquals("End time should match", end, event.getEndTime());
        assertEquals("Location should match", location, event.getLocation());
        assertEquals("QR Code should match", qrCode, event.getQrCode());
        assertEquals("Poster URL should match", poster, event.getPoster());
        assertEquals("Organizer ID should match", organizerID, event.getOrganizerID());
    }
}
