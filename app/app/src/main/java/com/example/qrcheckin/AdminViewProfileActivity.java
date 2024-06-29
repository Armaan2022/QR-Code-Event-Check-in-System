package com.example.qrcheckin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * AdminViewProfileActivity displays the profile information of a user in the admin panel.
 * It allows the admin to view the user's details and delete the user's profile if needed.
 */
public class AdminViewProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    TextView name;
    TextView email;
    TextView phone;
    TextView url;
    CircleImageView userImage;

    /**
     * Initializes the activity, retrieves profile information from the intent, and sets up the UI.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String profileName = intent.getStringExtra("profileName");
        String profilePhone = intent.getStringExtra("profilePhone");
        String profileEmail = intent.getStringExtra("profileEmail");
        String profileUrl = intent.getStringExtra("profileUrl");
        String userID = intent.getStringExtra("userID");
        String profileImageUrl = intent.getStringExtra("profileImageUrl");

        Log.d("phone", "Phone no: " + profilePhone);

        setContentView(R.layout.activity_admin_view_profile);

        Button Back = findViewById(R.id.button_back);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SecondActivity when the button is clicked
                Intent intent = new Intent(AdminViewProfileActivity.this, BrowseProfileAdmin.class);
                startActivity(intent);
            }
        });

        Button deleteprofile = findViewById(R.id.button_delete_profile);
        deleteprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteProfilefunction(userID);

            }
        });


        name = findViewById(R.id.user_name_view);
        email = findViewById(R.id.user_email_view);
        phone = findViewById(R.id.user_phone_view);
        url = findViewById(R.id.user_url_view);
        userImage = findViewById(R.id.profile_image);

        name.setText(profileName);
        phone.setText(profilePhone);
        email.setText(profileEmail);
        url.setText(profileUrl);
        assert profileImageUrl != null;
        Bitmap profileBitmap = Helpers.base64ToBitmap(profileImageUrl);
        userImage.setImageBitmap(profileBitmap);
    }

    /**
     * Deletes the user profile from the database, including associated events.
     * @param userId The unique id of the user whose profile is to be deleted.
     */
    private void DeleteProfilefunction(String userId) {
        // TODO:- Need to delete organized events and signed up events
        db.collection("user").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        ArrayList<String> organizedEvent = (ArrayList<String>) documentSnapshot.get("organizedEvent");
                        ArrayList<String> signedUpEvents = (ArrayList<String>) documentSnapshot.get("signedUpEvents");
                        // Delete the user's organized events
                        if (organizedEvent != null) {
                            for (String eventID : organizedEvent) {
                                db.collection("event").document(eventID).delete();
                            }
                        }

                        // Remove the user from the signedUpAttendee list in each event they signed up for
                        if (signedUpEvents != null) {

                            for (String eventID : signedUpEvents) {
                                DocumentReference DocRef = db.collection("event").document(eventID);

                                DocRef.get().addOnSuccessListener(eventDocumentSnapshot -> {
                                    HashMap<String, Object> data = new HashMap<>();

                                    // Remove user from SignedupAttendee list
                                    ArrayList<String> signedUpAttendees = (ArrayList<String>) eventDocumentSnapshot.get("signedUpAttendees");
                                    if (signedUpAttendees!=null) {
                                        signedUpAttendees.remove(userId);
                                        data.put("signedUpAttendees", signedUpAttendees);
                                    }
                                    // Decrement the number of signedup attendees
                                    Long countSignup = eventDocumentSnapshot.getLong("countSignup");
                                    if (countSignup!=null){
                                        Log.e("Debug", "countSignup: " + countSignup);
                                        countSignup -= 1;
                                        data.put("countSignup", countSignup);
                                    }
                                    DocRef.update(data);
                                });
                            }
                            db.collection("user").document(userId).delete();
                            Intent intent = new Intent(AdminViewProfileActivity.this, BrowseProfileAdmin.class);
                            startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failures
                    Log.e("TAG", "Error retrieving document", e);
                });
    }
}
