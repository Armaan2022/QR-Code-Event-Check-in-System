package com.example.qrcheckin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This activity provides functionality for users to view events they have signed up for.
 * It fetches event data from Firebase Firestore, displaying each event in a list.
 */
public class SignedUpEventActivity extends AppCompatActivity {

    Button home;
    Button checkIn;

    CircleImageView profile;

    private ArrayList<Event> dataList;
    private ListView eventList;
    private SignedUpEventAdapter eventAdapter;
    private FirebaseFirestore db;

    private String mainUserID;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signed_up_events);

        db = FirebaseFirestore.getInstance();

        getSignedUpEvents();

        try {
            FileInputStream fis = openFileInput("localStorage.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            mainUserID = sb.toString();
            Log.d("Main USER ID", mainUserID);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mainUserID !=null){
            fetchDetails(mainUserID);
        }


        home = findViewById(R.id.button_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //feel free to use the code below to connect to the activity
                Intent intent = new Intent(SignedUpEventActivity.this, HomepageActivity.class);// go to event activity need to connect with other activity
                startActivity(intent);
            }
        });
        //click the check in button
        checkIn = findViewById(R.id.button_check_in);
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignedUpEventActivity.this, QRScannerActivity.class);// go to event activity need to connect with other activity
                startActivity(intent);
            }
        });

        profile = findViewById(R.id.profile_image_button);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignedUpEventActivity.this, ProfileActivity.class);// go to event activity need to connect with other activity
                startActivity(intent);
            }
        });

        dataList = new ArrayList<>();
        eventList = findViewById(R.id.event_list);
        eventAdapter = new SignedUpEventAdapter(this, dataList);
        eventList.setAdapter(eventAdapter);

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the clicked event
                Event clickedEvent = dataList.get(position);

                // Create an Intent to start the new activity
                Intent intent = new Intent(SignedUpEventActivity.this, ViewEventActivity.class);

                // Pass data to the eventDetail activity
                intent.putExtra("eventName", clickedEvent.getName()); // get name
                //intent.putExtra("organizerName", clickedEvent.getOrganizerID()); // And a getOrganizerName method
                intent.putExtra("startTime", clickedEvent.getStartTime());
                intent.putExtra("endTime", clickedEvent.getEndTime());
                intent.putExtra("eventDes", clickedEvent.getDescription());
                intent.putExtra("location", clickedEvent.getLocation());
                intent.putExtra("poster",clickedEvent.getPoster());
                intent.putExtra("qr",clickedEvent.getQrCode());
                intent.putExtra("promoqr",clickedEvent.getPromoQR());
                intent.putExtra("origin", "signedinattendee");

                // Add other event details as needed

                // Start the
                startActivity(intent);
            }
        });

    }

    /**
     * Fetches and displays the events the user has signed up for.
     * Reads the user's ID from a local file,and updates the UI accordingly by fetching from
     * Firestore
     */

    private void fetchDetails(String uID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(uID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                // OpenAI, 2024, ChatGPT, Convert string to Bitmap
                String profileImage = documentSnapshot.getString("profileImage");
                Bitmap profileBitmap = Helpers.base64ToBitmap(profileImage);
                profile.setImageBitmap(profileBitmap);
            }
            else{
                Log.e("ProfileActivity", "No such document");
            }
        }).addOnFailureListener(error->{
            Log.e("ProfileActivity", "Error fetching document", error);
        });
    }

    /**
     * Fetches the list of events that the user has signed up to. It reads the
     * user's ID from a local storage and updates the list view with these events.
     */
    private void getSignedUpEvents() {

        try {
            FileInputStream fis = openFileInput("localStorage.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            mainUserID = sb.toString();
            Log.d("Main USER ID", mainUserID);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mainUserID != null) {
            db.collection("user").document(mainUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.e("FirestoreError", "Error getting event details", error);
                        return;
                    }
                    Log.d("FirestoreSuccess", "Successfully fetched events.");
                    dataList.clear();

                    // OpenAI, 2024, ChatGPT, Store a list of strings in a field in Firebase
                    if (value.exists()) {
                        ArrayList<String> eventIds = (ArrayList<String>) value.get("signedUpEvents");
                        if (eventIds != null && !eventIds.isEmpty()) {
                            for (String eventId : eventIds) {
                                // Get reference to the Event document using eventId
                                DocumentReference eventRef = db.collection("event").document(eventId);
                                eventRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot doc) {
                                        if (doc.exists()) {
                                            String eventName = doc.getString("eventName");
                                            String eventDes = doc.getString("eventDescription");
                                            String startTime = doc.getString("startTime");
                                            String endTime = doc.getString("endTime");
                                            String location = doc.getString("location");
                                            String poster = doc.getString("poster");
                                            String qr = doc.getString("checkinQRCode");
                                            String promoqr = doc.getString("promoQRCode");

                                            Event event = new Event();
                                            event.setName(eventName);
                                            event.setDescription(eventDes);
                                            event.setStartTime(startTime);
                                            event.setEndTime(endTime);
                                            event.setLocation(location);
                                            event.setPoster(poster);
                                            event.setQrCode(qr);
                                            if (promoqr != null) {
                                                event.setPromoQR(promoqr);
                                            }
                                            dataList.add(event);
                                        }
                                        eventAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
        else{
            Log.d("DEBUG", "User ID not found");
        }
    }
}
