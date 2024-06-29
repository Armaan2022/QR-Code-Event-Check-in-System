package com.example.qrcheckin.SendNotificationPack;

/**
 * Represents the message structure of the notification to be sent
 */
public class Data {
    private String Message;

    public Data(String message) {
        Message = message;
    }
    public Data() {}

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
