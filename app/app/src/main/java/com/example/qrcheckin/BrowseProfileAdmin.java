package com.example.qrcheckin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
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
/**
 * Activity for browsing profiles of the application.
 * It allows the admins to view a list of profiles and get detailed information about a profile.
 */
public class BrowseProfileAdmin extends AppCompatActivity {

    private FirebaseFirestore db;

    // Main content
    private ArrayList<Profile> dataList;
    private ListView eventList;
    private ProfileArrayAdapter eventAdapter;

    ImageButton back;
    String mainUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_profile_admin);

        Button viewevents = findViewById(R.id.button_admin_events);
        viewevents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SecondActivity when the button is clicked
                Intent intent = new Intent(BrowseProfileAdmin.this, HomepageAdmin.class);
                startActivity(intent);
            }
        });

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

        back = findViewById(R.id.button_backArrow);

        back.setOnClickListener(v -> {

            Intent intent = new Intent(BrowseProfileAdmin.this, HomepageAdmin.class);
            startActivity(intent);
        });

        Button viewimages = findViewById(R.id.button_admin_images);
        viewimages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SecondActivity when the button is clicked
                Intent intent = new Intent(BrowseProfileAdmin.this, BrowseImagesActivity.class);
                startActivity(intent);
            }
        });

        db = FirebaseFirestore.getInstance();

        // Reference to your collection
        CollectionReference collectionRef = db.collection("user");

        getProfile();


        dataList = new ArrayList<>();
        eventList = findViewById(R.id.event_list);
        Date startTime = new Date(); // Current time
        // Assuming the event ends in 2 hours from the current time


        eventAdapter = new ProfileArrayAdapter(this, dataList);
        eventList.setAdapter(eventAdapter);

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the clicked event
                Profile clickedEvent = dataList.get(position);

                // Create an Intent to start the new activity
                Intent intent = new Intent(BrowseProfileAdmin.this, AdminViewProfileActivity.class);

                // Pass data to the eventDetail activity
                intent.putExtra("profileName", clickedEvent.getUserName()); // get name
                intent.putExtra("profileEmail", clickedEvent.getEmail());
                intent.putExtra("profilePhone", clickedEvent.getPhone());
                intent.putExtra("profileUrl", clickedEvent.getHomepage());
                intent.putExtra("userID", clickedEvent.getUserID());
                intent.putExtra("profileImageUrl", clickedEvent.getProfileImageUrl());
                Log.d("phone", "Phone no: " + clickedEvent.getPhone());

                // Start the
                startActivity(intent);
            }
        });

    }
    /**
     * Fetches user profiles from Firestore and updates the ListView with the fetched profiles.
     */
    private void getProfile() {
        db.collection("user").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                    String profileName = doc.getString("name");
                    String profileEmail = doc.getString("email");
                    String profilePhone = doc.getString("phone");
                    String profileUrl = doc.getString("url");
                    String profileImageUrl = doc.getString("profileImage");
                    String userID = doc.getId();

                    if (userID.equals(mainUserID)) {
                        continue;
                    }

                    Log.d("-->phone", "Phone no: " + profilePhone);

                    Profile profile = new Profile(profileName, profilePhone, profileEmail, profileUrl);
                    profile.setUserID(userID);
                    profile.setProfileImageUrl(profileImageUrl);

                    dataList.add(profile);
                }
                eventAdapter.notifyDataSetChanged();
            }
        });
    }
}