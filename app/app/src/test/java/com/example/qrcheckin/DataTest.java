package com.example.qrcheckin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.qrcheckin.SendNotificationPack.Data;

import org.junit.Before;
import org.junit.Test;
//test for checking the Data class used in sending a notification message
public class DataTest {

    private Data data;

    @Before
    public void setUp() {
        data = new Data();
    }

    @Test
    public void getMessage_NullMessage_ReturnsNull() {
        // Assert that initially, the message is null
        assertNull(data.getMessage());
    }

    @Test
    public void setMessage_SetMessage_ReturnsSetMessage() {
        // Arrange
        String message = "Test message";

        // Act
        data.setMessage(message);

        // Assert
        assertEquals(message, data.getMessage());
    }
}
