package com.example.qrcheckin.SendNotificationPack;

/**
 * A class representing a notification sender.
 */
public class NotificationSender {

    public Data data;
    public String to;

    /**
     * Constructs a new NotificationSender object with the specified data and recipient.
     * @param data The data associated with the notification sender.
     * @param to The recipient of the notification.
     */
    public NotificationSender(Data data, String to) {
        this.data = data;
        this.to = to;
    }
    public NotificationSender() {}
}
