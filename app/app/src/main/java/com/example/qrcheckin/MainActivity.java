package com.example.qrcheckin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileOutputStream;

/**
 * MainActivity is the starting point of the application. It is responsible for initializing
 * the Firestore database instance, checking if the user profile exists based on the Android device ID.
 */

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private AppBarConfiguration appBarConfiguration;
    //private ActivityMainBinding binding;
    String android_id;
    String mainUserID;
    boolean doesExist;
    String isLocationOn = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        try {
            FileOutputStream fos = openFileOutput("locationStatus.txt", Context.MODE_PRIVATE);
            fos.write(isLocationOn.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkIsUserProfile();
    }

    /**
     * Checks if the user profile exists in the Firestore database based on the device's unique ID.
     * Move to the relevant activity depending on the result
     */
    private void checkIsUserProfile() {
        // Stackoverflow, 2024, Source: https://stackoverflow.com/questions/16869482/how-to-get-unique-device-hardware-id-in-android
        android_id = Settings.Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        Log.d("THIS IS MY A ID ", android_id);
        db.collection("user").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!= null){
                    Log.e("FirestoreError", "Error getting event details",error);
                    return;
                }
                Log.d("FirestoreSuccess", "Successfully fetched events.");
                doesExist = false;
                assert value != null;
                for(QueryDocumentSnapshot doc: value){
                    String deviceID = doc.getString("androidID");
                    if (deviceID == null){
                        // Do nothing
                    }
                    else if (deviceID.equals(android_id)){
                        mainUserID = doc.getId();
                        doesExist = true;
                    }
                }
                if (doesExist){
                    Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
                    //intent.putExtra("UserID", doc.getId()); // Attach the bundle to the intent
                    try {
                        FileOutputStream fos = openFileOutput("localStorage.txt", Context.MODE_PRIVATE);
                        fos.write(mainUserID.getBytes());
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
