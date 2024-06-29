package com.example.qrcheckin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Represents an activity for displaying and editing a user's profile.
 * It fetches the user's profile details from Firebase Firestore and displays them.
 */
public class ProfileActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView userName;
    TextView userEmail;
    TextView userPhone;
    TextView userUrl;
    CircleImageView userImage;

    //ImageView userImage;
    Button back;
    Button edit;

    boolean isAdmin;
    Button adminButton;

    String mainUserID;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        userName = findViewById(R.id.user_name_view);
        userEmail = findViewById(R.id.user_email_view);
        userPhone = findViewById(R.id.user_phone_view);
        userUrl = findViewById(R.id.user_url_view);
        userImage = findViewById(R.id.profile_image);
        back = findViewById(R.id.button_back);

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

        //edit button
        edit = findViewById(R.id.button_edit_events);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //feel free to use the code below to connect to the activity
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);// go to event activity need to connect with other activity
                intent.putExtra("UserID",mainUserID);
                startActivity(intent);
            }
        });
        //back = findViewById(R.id.button_back_events);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //feel free to use the code below to connect to the activity
                Intent intent = new Intent(ProfileActivity.this, HomepageActivity.class);// go to event activity need to connect with other activity
                startActivity(intent);
            }
        });

        adminButton = findViewById(R.id.button_admin);
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdmin) {
                    Intent intent = new Intent(ProfileActivity.this, HomepageAdmin.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProfileActivity.this, "You are not an admin !", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Fetches and displays the user's profile details from Firestore based on the provided user ID.
     * If the user's details exist in Firestore, it updates the UI with the fetched information.
     *
     * @param uID The user ID whose profile details are to be fetched.
     */
    private void fetchDetails(String uID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(uID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                String profileName = documentSnapshot.getString("name");
                String profilePhone = documentSnapshot.getString("phone");
                String profileEmail = documentSnapshot.getString("email");
                String profileUrl = documentSnapshot.getString("url");
                String profileImage = documentSnapshot.getString("profileImage");
                Bitmap profileBitmap = Helpers.base64ToBitmap(profileImage);
                isAdmin = Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"));
                userImage.setImageBitmap(profileBitmap);
                userName.setText(profileName);
                userEmail.setText(profileEmail);
                userPhone.setText(profilePhone);
                userUrl.setText(profileUrl);
            }
            else{
                Log.e("ProfileActivity", "No such document");
            }
        }).addOnFailureListener(error->{
            Log.e("ProfileActivity", "Error fetching document", error);
        });
    }

}
