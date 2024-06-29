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

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The HomepageActivity class initializes the main homepage application,
 * providing users with the option to organize events, sign up for events,
 * check in,and access their profile.
 */

public class HomepageAdmin extends AppCompatActivity {

    Button organizeEvent;
    Button signedUp;
    Button checkIn;
    ImageButton notification;
    ImageButton local;
    CircleImageView profile;
    private FirebaseFirestore db;
    private ArrayList<Event> dataList;
    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    String mainUserID;
    Button homeButton;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_admin);


        Button viewprofile = findViewById(R.id.button_admin_profiles);
        viewprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SecondActivity when the button is clicked
                Intent intent = new Intent(HomepageAdmin.this, BrowseProfileAdmin.class);
                startActivity(intent);
            }
        });
        Button viewimages = findViewById(R.id.button_admin_images);
        viewimages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SecondActivity when the button is clicked
                Intent intent = new Intent(HomepageAdmin.this, BrowseImagesActivity.class);
                startActivity(intent);
            }
        });

        homeButton = findViewById(R.id.button_home);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomepageAdmin.this, HomepageActivity.class);
                startActivity(intent);
            }
        });

        db = FirebaseFirestore.getInstance();

        getEvent();


        //String uID = getIntent().getStringExtra("UserID");

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

        Intent intent = getIntent();


        dataList = new ArrayList<>();
        eventList = findViewById(R.id.event_list);
        Date startTime = new Date(); // Current time
        // Assuming the event ends in 2 hours from the current time


        eventAdapter = new EventArrayAdapter(this, dataList);
        eventList.setAdapter(eventAdapter);

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the clicked event
                Event clickedEvent = dataList.get(position);

                // Create an Intent to start the new activity
                Intent intent = new Intent(HomepageAdmin.this, ViewEventActivity.class);

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
                intent.putExtra("origin", "admin");
                intent.putExtra("EventID", clickedEvent.getEventID());


                // Add other event details as needed

                // Start the
                startActivity(intent);
            }
        });

    }

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
     * Retrieves the list of events from the Firestore database
     * and updates the lists accordingly.
     */
    private void getEvent() {
        db.collection("event").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!= null){
                    Log.e("FirestoreError", "Error getting event details",error);
                    return;
                }
                Log.d("FirestoreSuccess", "HomepageActivity Successfully fetched events.");
                dataList.clear();

                assert value != null;
                for(QueryDocumentSnapshot doc: value){
                    String eventName = doc.getString("eventName");
                    String eventDes = doc.getString("eventDescription");
                    String startTime = doc.getString("startTime");
                    String endTime = doc.getString("endTime");
                    String location = doc.getString("location");
                    String poster = doc.getString("poster");
                    String qr = doc.getString("checkinQRCode");
                    String promoqr = doc.getString("promoQRCode");
                    String EventID = doc.getId();
                    Event event = new Event();
                    event.setName(eventName);
                    event.setDescription(eventDes);
                    event.setStartTime(startTime);
                    event.setEndTime(endTime);
                    event.setLocation(location);
                    event.setPoster(poster);
                    event.setQrCode(qr);
                    event.setEventID(EventID);
                    if (promoqr!= null){
                        event.setPromoQR(promoqr);
                    }

                    dataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }
        });
    }

}
