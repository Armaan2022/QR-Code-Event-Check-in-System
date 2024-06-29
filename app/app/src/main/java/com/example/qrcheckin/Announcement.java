package com.example.qrcheckin;

/**
 * This class defines Announcements.
 * It is responsible for holding the announcements in form of a string
 */
public class Announcement {
    private String announcement;

    public Announcement(String announcement) {
        this.announcement = announcement;
    }

    /**
     * Returns the text content of this announcement.
     *
     * @return announcement.
     */
    public String getAnnouncement() {
        return announcement;
    }

    /**
     * Sets the  announcement.
     *
     * @param announcement The content to be set for the announcement.
     */
    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }
}
