package com.example.qrcheckin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The HomepageActivity class initializes the main homepage application,
 * providing users with the option to organize events, sign up for events,
 * check in,and access their profile.
 */
public class HomepageActivity extends AppCompatActivity {
    public boolean isUnderInstrumentationTest() {
        try {
            Class.forName("androidx.test.espresso.Espresso");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    Button organizeEvent;
    Button signedUp;
    Button checkIn;
    CircleImageView profile;
    private FirebaseFirestore db;
    private SwitchCompat switchGeolocation;

    private ArrayList<Event> dataList;
    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    String mainUserID;
    String isLocationOn;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        switchGeolocation = findViewById(R.id.switch_geolocation);
        if (!isUnderInstrumentationTest()) {
            checkAndRequestLocationPermissions();
        }

        db = FirebaseFirestore.getInstance();

        getEvent();

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

        // Get the current registration token for the device
        getUserToken(mainUserID);

        try {
            FileInputStream fis = openFileInput("locationStatus.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            isLocationOn = sb.toString();
            Log.d("Location Status", isLocationOn);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (mainUserID !=null){
            fetchDetails(mainUserID);
        }

        //click the organizeEvent button
        organizeEvent = findViewById(R.id.button_organize_events);
        organizeEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //feel free to use the code below to connect to the activity
                Intent intent = new Intent(HomepageActivity.this, HomepageOrganizer.class);// go to event activity need to connect with other activity
                startActivity(intent);
            }
        });

        checkIn = findViewById(R.id.button_check_in);
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomepageActivity.this, QRScannerActivity.class);
                startActivity(intent);
            }
        });

        //click the signedUp button
        signedUp = findViewById(R.id.button_signed_up_events);
        signedUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomepageActivity.this, SignedUpEventActivity.class);// go to event activity need to connect with other activity
                startActivity(intent);
            }
        });
        Intent intent = getIntent();

        String profileImage = intent.getStringExtra("profileImage");
        String profName = intent.getStringExtra("name");
        String profEmail = intent.getStringExtra("email");
        String profPhone = intent.getStringExtra("phone");
        String profUrl = intent.getStringExtra("url");

        profile = findViewById(R.id.profile_image_button);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(HomepageActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

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
                Intent intent = new Intent(HomepageActivity.this, ViewEventActivity.class);

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
                intent.putExtra("origin", "attendee");

                // Start the
                startActivity(intent);
            }
        });

        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean geolocationEnabled = prefs.getBoolean("GeolocationEnabled", false);
        switchGeolocation.setChecked(geolocationEnabled);

        // Show toast to allow notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestNotificationPermission();
        }

        // OpenAI, 2024, ChatGPT, How to add a toggle switch
        switchGeolocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                if (isChecked) {
                    // Check for location permission
                    if (ContextCompat.checkSelfPermission(HomepageActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // If permission not granted, request it
                        ActivityCompat.requestPermissions(HomepageActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    } else {
                        // Permission already granted, enable geolocation tracking
                        editor.putBoolean("GeolocationEnabled", true);
                        editor.apply();
                        // Show toast message
                        Toast.makeText(HomepageActivity.this, "Geolocation tracking is enabled.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Disable geolocation tracking
                    editor.putBoolean("GeolocationEnabled", false);
                    editor.apply();
                    // Show toast message
                    Toast.makeText(HomepageActivity.this, "Geolocation tracking is disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Asks the user to allow notification in order to receive push notifications
     */
    private void requestNotificationPermission() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (!notificationManager.areNotificationsEnabled()) {
            Toast.makeText(HomepageActivity.this, "Allow notifications to receive updates", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Requests location permissions from the user if not granted before.
     * Location permissions are needed for the map functionality.
     */

    // OpenAI, 2024, ChatGPT, How to ask for location permission
    private void checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    /**
     * Saves the user's preference for geolocation tracking within the application.
     * This preference is stored on the device
     * @param isEnabled Indicates whether geolocation is enabled or disabled by the user.
     */

    //// OpenAI, 2024, ChatGPT, How to save preferences in the device
    private void saveGeolocationPreference(boolean isEnabled) {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("GeolocationEnabled", isEnabled);
        editor.apply();
    }

    /**
     * Updates the geolocation preference based on the option selected by the user.
     * @param requestCode  The integer request code for the location
     * @param permissions  The requested permissions
     * @param grantResults The results which can be granted or not granted based on choice of the user.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, save the preference
            saveGeolocationPreference(true);
            switchGeolocation.setChecked(true);
        } else {
            // Permission denied, revert the switch to off
            switchGeolocation.setChecked(false);
            saveGeolocationPreference(false);
        }
    }
    /**
     * Fetches information about the user's profile from Firestore.
     * Retrieves the profile image and other user details to display.
     * @param uID The unique ID of the user to access Firebase document.
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
                    Event event = new Event();
                    event.setName(eventName);
                    event.setDescription(eventDes);
                    event.setStartTime(startTime);
                    event.setEndTime(endTime);
                    event.setLocation(location);
                    event.setPoster(poster);
                    event.setQrCode(qr);
                    if (promoqr!= null){
                        event.setPromoQR(promoqr);
                    }


                    dataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Updates the user's device token in Firestore for push notification functionality.
     * This method fetches the current token for the device.
     * @param mainUserID The unique ID of the user to access Firebase.
     */
    private void getUserToken(String mainUserID) {
        // Get the current registration token for the device
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("token", token);

                        // Store the user token in firebase
                        if (mainUserID!=null){
                            db.collection("user").document(mainUserID).update(userInfo).addOnCompleteListener(result -> {
                                if (result.isSuccessful()) {
                                    Log.d("TOKEN", "Token updated successfully");

                                } else {
                                    Log.e("TOKEN", "Token not updated successfully");
                                    // Optionally show an error message to the user
                                }
                            });
                        }
                        else{
                            Log.d("DEBUG", "User ID not found");
                        }
                        // Log and toast
                        Log.d("TOKEN", token);
                    }
                });
    }
}
