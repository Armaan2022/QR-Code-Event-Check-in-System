package com.example.qrcheckin;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;

import java.util.ArrayList;

public class SignedUpAdapterTest {
    private SignedUpEventAdapter adapter;
    private ArrayList<Event> event;

    @Before
    public void setUp() {
        event = new ArrayList<>();
        event.add(new Event("Event", "Description", "Start Time", "End Time", "Location", "QRCode", "", "", "OrganizerID"));
        event.get(0).setCheckInStatus(true);

        adapter = new SignedUpEventAdapter(ApplicationProvider.getApplicationContext(), event);
    }
}
