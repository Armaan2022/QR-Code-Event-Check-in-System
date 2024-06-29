package com.example.qrcheckin;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents an event in the application. This class has event details
 * such name, description, timings, etc. Also contains the getters and setters
 * for  different attributes.
 */

public class Event implements Serializable {
    private String eventID; // Use for Firebase document reference
    private String name;
    private String description; // TODO - Add to figma etc
    private String startTime;
    private String endTime;
    private String Location;

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPromoQR() {
        return promoQR;
    }

    public void setPromoQR(String promoQR) {
        this.promoQR = promoQR;
    }

    private String qrCode;
    private String poster;
    private String promoQR;
    private Integer attendeeCapacity;
    private String organizerID; // ID string for Firebase (use built in firebase functionality to generate this)
    private ArrayList<Profile> SignedUpAttendees;
    private ArrayList<Profile> CheckedInAttendees;

    private ArrayList<Integer> MileStone;


    // TODO private ArrayList<Announcement> announcements;

    private boolean CheckInStatus = false;

    /**
     * The constructor for the Event Class
     */
    public Event(){

    }

    // Constructor no attendee capacity with promo
    public Event(String eventName, String description, String start, String end, String Location, String qrCode, String promoQR, String poster, String organizerID){
        this.name = eventName;
        this.description = description;
        this.startTime = start;
        this.endTime = end;
        this.Location = Location;
        this.qrCode = qrCode;
        this.promoQR = promoQR;
        this.poster = poster;
        this.organizerID = organizerID;
    }

    // Constructor no attendee capacity without promo
    public Event(String eventName, String description, String start, String end, String Location, String qrCode, String poster, String organizerID){
        this.name = eventName;
        this.description = description;
        this.startTime = start;
        this.endTime = end;
        this.Location = Location;
        this.qrCode = qrCode;
        this.poster = poster;
        this.organizerID = organizerID;
    }

    // Method overload for when number of attendees is OPTIONALLY limited with promo
    public Event(String eventName, String description, String start, String end, String Location, Integer attendeeCapacity, String qrCode, String promoQR, String poster, String organizerID){
        this.name = eventName;
        this.description = description;
        this.startTime = start;
        this.endTime = end;
        this.Location = Location;
        this.attendeeCapacity = attendeeCapacity;
        this.qrCode = qrCode;
        this.promoQR = promoQR;
        this.poster = poster;
        this.organizerID = organizerID;
    }

    // Method overload for when number of attendees is OPTIONALLY limited without promo
    public Event(String eventName, String description, String start, String end, String Location, Integer attendeeCapacity, String qrCode, String poster, String organizerID){
        this.name = eventName;
        this.description = description;
        this.startTime = start;
        this.endTime = end;
        this.Location = Location;
        this.attendeeCapacity = attendeeCapacity;
        this.qrCode = qrCode;
        this.poster = poster;
        this.organizerID = organizerID;
    }

    /**
     * Helper method that checks if the event has reached capacity
     * @return boolean
     */
    public boolean reachedCapacity(){
        if(attendeeCapacity - SignedUpAttendees.size() == 0 ){
            return true;
        }
        else{
            return false;
        }

    }
/*
    /**
     * Signs up attendee to Signed Up Attendee list, as long as there is space.
     * If there is no space it throws an exception
     * @param attendee the attendee
     * @throws Exception Capacity reached
     */
    /*
    public void signUpAttendee(Profile attendee) throws Exception {
        if(reachedCapacity()){
            throw new Exception("Event has reached capacity");
        }
        else {
            SignedUpAttendees.add(attendee);
        }
    }*/


    // Getters & Setters
    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public Integer getAttendeeCapacity() {
        return attendeeCapacity;
    }

    public void setAttendeeCapacity(Integer attendeeCapacity) {
        this.attendeeCapacity = attendeeCapacity;
    }

    public ArrayList<Profile> getSignedUpAttendees() {
        return SignedUpAttendees;
    }

    public void setSignedUpAttendees(ArrayList<Profile> signedUpAttendees) {
        SignedUpAttendees = signedUpAttendees;
    }

    public ArrayList<Profile> getCheckedInAttendees() {
        return CheckedInAttendees;
    }

    public void setCheckedInAttendees(ArrayList<Profile> checkedInAttendees) {
        CheckedInAttendees = checkedInAttendees;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public void setOrganizerID(String organizerID) {
        this.organizerID = organizerID;
    }

    public boolean isCheckInStatus() {
        return CheckInStatus;
    }

    public void setCheckInStatus(boolean checkInStatus) {
        CheckInStatus = checkInStatus;
    }
}

