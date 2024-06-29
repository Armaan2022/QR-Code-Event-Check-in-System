package com.example.qrcheckin;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrcheckin.SendNotificationPack.APIService;
import com.example.qrcheckin.SendNotificationPack.Client;
import com.example.qrcheckin.SendNotificationPack.Data;
import com.example.qrcheckin.SendNotificationPack.MyResponse;
import com.example.qrcheckin.SendNotificationPack.NotificationSender;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Bing, 2024, source:https://www.bing.com/videos/riverview/relatedvideo?q=open%20camera%20scan%20qr%20code%20in%20android%20studio&mid=27B08E2657DEFA5CC74327B08E2657DEFA5CC743&ajaxhist=0
/**
 * Provides an interface to handle asynchronous behaviour from Firebase .
 */
interface PromoCodeCheckListener {
    void onPromoCodeChecked(boolean exists);
}
/**
 * Activity for scanning QR codes related to events. It provides functionality
 * for users to scan QR codes to check in to events  with Firestore integration
 * for verifying and updating event attendance.
 */
public class QRScannerActivity extends AppCompatActivity {

    String userID;
    String qrContent;

    FirebaseFirestore db;

    private APIService apiService;

    final int[] attendeeCapacityWrapper = new int[1];


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.qr_scanner);
        setContentView(R.layout.homepage);
        // Fetch db
        db = FirebaseFirestore.getInstance();

        scanCode();
    }
    /**
     * Sets up and launches the QR code scanner for checking into an event or
     * scanning a promo qr code.
     */
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    /**
     * Handles the result from scanning a QR code. If a QR code is successfully
     * scanned, it checks the Firestore database for event attendance, and
     * updates the database accordingly.
     */
    private ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        // OpenAI, 2024, ChatGPT, How to update and update increment arrays in firestore
        try {
            FileInputStream fis = openFileInput("localStorage.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            userID = sb.toString();
            Log.d("Main USER ID", userID);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // the lock for getting the check of the promo code

        if(result.getContents() != null) {

            qrContent = result.getContents();

            if(isValidDocumentId(qrContent)) {
                Log.e("debug", "check "+isValidDocumentId(qrContent));
                requestLocationUpdates(qrContent);

                AlertDialog.Builder builder = new AlertDialog.Builder(QRScannerActivity.this);

                // TODO Query Database to find Event
                // Specify the collection and document ID
                DocumentReference docRef = db.collection("event").document(qrContent);

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            // Check if the document contains the attendeeCapacity field
                            if (document.exists() && document.contains("attendeeCapacity")) {
                                // Get the value of attendeeCapacity
                                Number tempCapacity = document.getLong("attendeeCapacity"); // Firestore stores numbers as Long by default
                                Map<String, Long> userIDCheckIn = (Map<String, Long>) document.get("userIDCheckIn");
                                if (userIDCheckIn == null) {
                                    userIDCheckIn = new HashMap<>();
                                }
                                //get the current count from fire base
                                // reference: https://javatutorialhq.com/java/util/hashmap-class/getordefault-method-example/
                                Long currentCheckInCount = userIDCheckIn.getOrDefault(userID, 0L);
                                //if the current checkIn number is bigger than 1, it means we already checked in
                                boolean isCheckedIn = currentCheckInCount > 0;

                                //get the list of signedUp attendees
                                List<String> signedUpUsers = (List<String>) document.get("signedUpUsers");
                                if (signedUpUsers == null) signedUpUsers = new ArrayList<>();
                                // null = 0, not null = number of signed up
                                Set<String> uniqueAttendeeIDs = new HashSet<>();

                                // Check if signedUpUsers is not null before adding all to the HashSet
                                uniqueAttendeeIDs.addAll(signedUpUsers);

                                uniqueAttendeeIDs.addAll(userIDCheckIn.keySet());
                                int uniqueAttendees = uniqueAttendeeIDs.size();
                                // deal with the case where attendeeCapacity does not exist
                                if (tempCapacity != null) {
                                    attendeeCapacityWrapper[0] = tempCapacity.intValue();
                                    Log.d("Firestore", "Attendee Capacity: " + attendeeCapacityWrapper[0]);
                                }
                                if (isCheckedIn || uniqueAttendees < attendeeCapacityWrapper[0] || Objects.requireNonNull(signedUpUsers).contains(userID)) {
                                    if (!signedUpUsers.contains(userID) && !isCheckedIn) {
                                        // User is not on the signed-up list,
                                        signUpCount(qrContent); // This should increment your uniqueAttendees count
                                    }
                                    // If the user has already checked in or adding them does not exceed capacity
                                    userIDCheckIn.put(userID, currentCheckInCount + 1); // Increment or set their check-in count
                                    if(!isCheckedIn) checkMilestone(qrContent);

                                    docRef.update("userIDCheckIn", userIDCheckIn)
                                            .addOnSuccessListener(unused -> {

                                                Toast.makeText(getApplicationContext(), "Checked in successfully!", Toast.LENGTH_LONG).show();
                                                updateUserIDCheck();
                                                finish();

                                                Log.d("Firestore", "Document successfully updated!");
                                            }).addOnFailureListener(e -> {
                                                Toast.makeText(getApplicationContext(), "Event is at full capacity. Cannot check in.", Toast.LENGTH_LONG).show();

                                                Log.w("Firestore", "Error updating document", e);
                                            });
                                } else if (!isCheckedIn && uniqueAttendees >= attendeeCapacityWrapper[0] && !signedUpUsers.contains(userID)) {
                                    // New attendee and capacity reached
                                    //Log.d("Firestore", "Cannot check-in. Event is at full capacity.");
                                    // Handle full capacity (e.g., show a message to the user)

                                    builder.setTitle("Cannot Checked In ! Reach to the max capacity");
                                    builder.setMessage(result.getContents());
                                    builder.setPositiveButton("OK", null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    Intent intent = new Intent(QRScannerActivity.this, HomepageActivity.class);
                                    startActivity(intent);
                                } else {

                                    checkMilestone(qrContent);
                                    updateUserIDCheck();

//                                    updateUserIDCheck();

                                    builder.setTitle("Checked In !");
                                    builder.setMessage(result.getContents());
                                    builder.setPositiveButton("OK", null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    Intent intent = new Intent(QRScannerActivity.this, HomepageActivity.class);
                                    startActivity(intent);
                                }
                            } else {
                                //promo code
                                checkPromoCode(exists -> {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(QRScannerActivity.this);
                                    if (exists) {
                                        // Handle the case where the promo code exists.
                                        // For example, navigate to another activity or show a success message.

                                        // Navigate to View Event Activity
                                        getEvent(Helpers.reverseString(qrContent), new EventCallback() {
                                            @Override
                                            public void onEventReceived(Event event) {
                                                // Handle the fetched event here
                                                Log.e("YourActivity", "Event fetched: " + event.getName());
                                                // Do whatever you need with the event object
                                                goToEvent(event);
                                            }
                                        });

                                    }
                                    else {
                                        // reused qr code
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        CollectionReference reusedQRCodeCollection = db.collection("reusedQRCodes");


                                        reusedQRCodeCollection.whereEqualTo("decodedString", qrContent)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                                            String documentId = document1.getId(); // This is the document ID

                                                            String eventID = document1.getString("eventID");
                                                            assert eventID != null;


                                                            Log.d("Firestore", "Document ID: " + documentId);
                                                            DocumentReference docRef1 = db.collection("event").document(eventID);
                                                            docRef1.get().addOnCompleteListener(task11 -> {
                                                                if (task11.isSuccessful()) {
                                                                    DocumentSnapshot document11 = task11.getResult();

                                                                    // Check if the document contains the attendeeCapacity field
                                                                    if (document11.exists() && document11.contains("attendeeCapacity")) {
                                                                        // Get the value of attendeeCapacity
                                                                        Number tempCapacity = document11.getLong("attendeeCapacity"); // Firestore stores numbers as Long by default
                                                                        Map<String, Long> userIDCheckIn = (Map<String, Long>) document11.get("userIDCheckIn");
                                                                        if (userIDCheckIn == null) {
                                                                            userIDCheckIn = new HashMap<>();
                                                                        }
                                                                        //get the current count from fire base
                                                                        // reference: https://javatutorialhq.com/java/util/hashmap-class/getordefault-method-example/
                                                                        Long currentCheckInCount = userIDCheckIn.getOrDefault(userID, 0L);
                                                                        //if the current checkIn number is bigger than 1, it means we already checked in
                                                                        boolean isCheckedIn = currentCheckInCount > 0;

                                                                        //get the list of signedUp attendees
                                                                        List<String> signedUpUsers = (List<String>) document11.get("signedUpUsers");
                                                                        // null = 0, not null = number of signed up
                                                                        Set<String> uniqueAttendeeIDs = new HashSet<>(signedUpUsers);
                                                                        uniqueAttendeeIDs.addAll(userIDCheckIn.keySet());
                                                                        int uniqueAttendees = uniqueAttendeeIDs.size();
                                                                        // deal with the case where attendeeCapacity does not exist
                                                                        if (tempCapacity != null) {
                                                                            attendeeCapacityWrapper[0] = tempCapacity.intValue();
                                                                            Log.d("Firestore", "Attendee Capacity: " + attendeeCapacityWrapper[0]);
                                                                        }
                                                                        if (isCheckedIn || uniqueAttendees < attendeeCapacityWrapper[0] || signedUpUsers.contains(userID)) {
                                                                            // If the user has already checked in or adding them does not exceed capacity
                                                                            userIDCheckIn.put(userID, currentCheckInCount + 1); // Increment or set their check-in count
                                                                            if(!isCheckedIn) checkMilestone(eventID);

                                                                            if (!signedUpUsers.contains(userID) && !isCheckedIn) {
                                                                                // User is not on the signed-up list, treat as walk-in
                                                                                signUpCount(eventID); // This should increment your uniqueAttendees count
                                                                            }

                                                                            docRef1.update("userIDCheckIn", userIDCheckIn)
                                                                                    .addOnSuccessListener(unused -> {


                                                                                        //signUpCount(qrContent);

                                                                                        Toast.makeText(getApplicationContext(), "Checked in successfully!", Toast.LENGTH_LONG).show();

                                                                                        Log.d("Firestore", "Document successfully updated!");
                                                                                    }).addOnFailureListener(e -> {
                                                                                        Toast.makeText(getApplicationContext(), "Event is at full capacity. Cannot check in.", Toast.LENGTH_LONG).show();

                                                                                        Log.w("Firestore", "Error updating document", e);
                                                                                    });
                                                                            builder1.setMessage(" Checked In !");
                                                                            builder1.setPositiveButton("OK", null);
                                                                            AlertDialog dialog = builder1.create();
                                                                            dialog.show();
                                                                        } else if (!isCheckedIn && uniqueAttendees >= attendeeCapacityWrapper[0]&& !signedUpUsers.contains(userID)) {
                                                                            // New attendee and capacity reached
                                                                            //Log.d("Firestore", "Cannot check-in. Event is at full capacity.");
                                                                            // Handle full capacity (e.g., show a message to the user)

                                                                            builder1.setTitle("Cannot Checked In ! Reach to the max capacity");
                                                                            builder1.setMessage(result.getContents());
                                                                            builder1.setPositiveButton("OK", null);
                                                                            AlertDialog dialog = builder1.create();
                                                                            dialog.show();
                                                                        } else {
                                                                            checkMilestone(eventID);
                                                                            updateUserIDCheck();
                                                                            builder1.setTitle("Checked In !");
                                                                            builder1.setMessage(result.getContents());
                                                                            builder1.setPositiveButton("OK", null);
                                                                            AlertDialog dialog = builder1.create();
                                                                            dialog.show();
                                                                        }
                                                                    }
                                                                }
                                                            });// You can now use the document ID as needed
                                                        }
                                                    } else {
                                                        Log.d("Firestore", "Error getting documents: ", task1.getException());
                                                    }
                                                });
//                                        builder1.setMessage("Reused QR Code. Content: " + qrContent);
//                                        builder1.setPositiveButton("OK", null);
//                                        AlertDialog dialog = builder1.create();
//                                        dialog.show();
                                    }
                                });
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }
                    }
                });
            }else{
                // checked in by reused qr code

                AlertDialog.Builder builder = new AlertDialog.Builder(QRScannerActivity.this);
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                CollectionReference reusedQRCodeCollection = db.collection("reusedQRCodes");


                reusedQRCodeCollection.whereEqualTo("decodedString", qrContent)
                        .get()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                    String documentId = document1.getId(); // This is the document ID

                                    String eventID = document1.getString("eventID");
                                    assert eventID != null;
                                    Log.d("Firestore", "Document ID: " + documentId);
                                    DocumentReference docRef1 = db.collection("event").document(eventID);
                                    docRef1.get().addOnCompleteListener(task11 -> {
                                        if (task11.isSuccessful()) {
                                            DocumentSnapshot document11 = task11.getResult();

                                            // Check if the document contains the attendeeCapacity field
                                            if (document11.exists() && document11.contains("attendeeCapacity")) {
                                                // Get the value of attendeeCapacity
                                                Number tempCapacity = document11.getLong("attendeeCapacity"); // Firestore stores numbers as Long by default
                                                Map<String, Long> userIDCheckIn = (Map<String, Long>) document11.get("userIDCheckIn");
                                                if (userIDCheckIn == null) {
                                                    userIDCheckIn = new HashMap<>();
                                                }
                                                //get the current count from fire base
                                                // reference: https://javatutorialhq.com/java/util/hashmap-class/getordefault-method-example/
                                                Long currentCheckInCount = userIDCheckIn.getOrDefault(userID, 0L);
                                                //if the current checkIn number is bigger than 1, it means we already checked in
                                                boolean isCheckedIn = currentCheckInCount > 0;

                                                //get the list of signedUp attendees
                                                List<String> signedUpUsers = document11.contains("signedUpUsers") ? (List<String>) document11.get("signedUpUsers") : new ArrayList<>();
                                                // null = 0, not null = number of signed up
                                                Set<String> uniqueAttendeeIDs = new HashSet<>(signedUpUsers);
                                                uniqueAttendeeIDs.addAll(userIDCheckIn.keySet());
                                                int uniqueAttendees = uniqueAttendeeIDs.size();
                                                // deal with the case where attendeeCapacity does not exist
                                                if (tempCapacity != null) {
                                                    attendeeCapacityWrapper[0] = tempCapacity.intValue();
                                                    Log.d("Firestore", "Attendee Capacity: " + attendeeCapacityWrapper[0]);
                                                }
                                                if (isCheckedIn || uniqueAttendees < attendeeCapacityWrapper[0] || signedUpUsers.contains(userID)) {
                                                    // If the user has already checked in or adding them does not exceed capacity
                                                    userIDCheckIn.put(userID, currentCheckInCount + 1); // Increment or set their check-in count
                                                    if(!isCheckedIn) checkMilestone(eventID);
                                                    if (!signedUpUsers.contains(userID) && !isCheckedIn) {
                                                        // User is not on the signed-up list, treat as walk-in
                                                        signUpCount(eventID); // This should increment your uniqueAttendees count
                                                    }
                                                    docRef1.update("userIDCheckIn", userIDCheckIn)
                                                            .addOnSuccessListener(unused -> {
                                                                //updateEventLocation(eventID, 53.5232493, -113.4652543, userID);

                                                                requestLocationUpdates(eventID);
                                                                Toast.makeText(getApplicationContext(), "Checked in successfully!", Toast.LENGTH_LONG).show();
                                                                updateUserIDCheck();
                                                                Intent intent = new Intent(QRScannerActivity.this, HomepageActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                                Log.d("Firestore", "Document successfully updated!");
                                                            }).addOnFailureListener(e -> {
                                                                Toast.makeText(getApplicationContext(), "Event is at full capacity. Cannot check in.", Toast.LENGTH_LONG).show();

                                                                Log.w("Firestore", "Error updating document", e);
                                                            });
//                                                    builder.setMessage(" Checked In !");
//                                                    builder.setPositiveButton("OK", null);
//                                                    AlertDialog dialog = builder.create();
//                                                    dialog.show();
                                                } else if (!isCheckedIn && uniqueAttendees >= attendeeCapacityWrapper[0] && !signedUpUsers.contains(userID)) {
                                                    // New attendee and capacity reached
                                                    //Log.d("Firestore", "Cannot check-in. Event is at full capacity.");
                                                    // Handle full capacity (e.g., show a message to the user)

                                                    builder.setTitle("Cannot Checked In ! Reach to the max capacity");
                                                    builder.setMessage(result.getContents());
                                                    builder.setPositiveButton("OK", null);
                                                    AlertDialog dialog = builder.create();
                                                    dialog.show();
                                                } else {
                                                    checkMilestone(eventID);
                                                    updateUserIDCheck();
                                                    builder.setTitle("Checked In !");
                                                    builder.setMessage(result.getContents());
                                                    builder.setPositiveButton("OK", null);
                                                    AlertDialog dialog = builder.create();
                                                    dialog.show();
                                                }
                                            }
                                        }
                                    });// You can now use the document ID as needed
                                }
                            } else {
                                Log.d("Firestore", "Error getting documents: ", task1.getException());
                            }
                        });
//                builder.setMessage("Reused QR Code. Content: " + qrContent);
//                builder.setPositiveButton("OK", null);
//                AlertDialog dialog = builder.create();
//                dialog.show();
            }

        }
    });

    /**
     * Requests updates for the device's check in location and updates the location in the Firebase.
     * @param qrContent The content of the scanned QR code, which is basically an event ID.
     */
    private void requestLocationUpdates(String qrContent) {
        // OpenAI, 2024, ChatGPT, How to get the current location of the device
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isGeolocationEnabled = prefs.getBoolean("GeolocationEnabled", false);

        if (isGeolocationEnabled) {


            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }

            FusedLocationProviderClient fusedLocationClient = getFusedLocationProviderClient(this);

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000); // 10 seconds
            locationRequest.setFastestInterval(5000); // 5 seconds
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // If there is a location result, take the first one and use it to update your Firestore.
            // Your method to update Firestore
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    // If there is a location result, take the first one and use it to update your Firestore.
                    android.location.Location location = locationResult.getLocations().get(0);
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    updateEventLocation(qrContent, latitude, longitude, userID); // Your method to update Firestore
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            Log.d("requestLocationUpdates", "Geolocation is disabled in preferences.");
            return;
        }
    }

    /**
     * Updates the location of the checked-in user to Firestore
     * @param qrContent The content of the scanned QR code, event ID.
     * @param latitude The latitude of the current location.
     * @param longitude The longitude of the current location.
     * @param userID The user ID of the attendee.
     */
    private void updateEventLocation(String qrContent, double latitude, double longitude, String userID) {
        if (qrContent == null || qrContent.isEmpty() || userID == null || userID.isEmpty()) {
            Log.e("UpdateEventLocation", "QR Content or User ID is null or empty.");
            return;
        }
        // OpenAI, 2024, ChatGPT, Store the location values in a map of a map

        DocumentReference locationDocRef = db.collection("event").document(qrContent);

        // Prepare the nested location data for the user
        Map<String, Object> userLocation = new HashMap<>();
        userLocation.put("latitude", latitude);
        userLocation.put("longitude", longitude);

        // Prepare the update for the checkIns map, using dot notation for nested fields
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("checkIns." + userID, userLocation);

        // Perform the update
        locationDocRef.update(updateData)
                .addOnSuccessListener(aVoid -> Log.d("UpdateEventLocation", "User location successfully updated in event."))
                .addOnFailureListener(e -> Log.e("UpdateEventLocation", "Error updating user location in event.", e));
    }

    /**
     * Validates if a given string is a valid ID or not.
     * @param id The string to validate.
     * @return True if the string is a valid document ID.
     */
    public boolean isValidDocumentId(String id) {
        // A basic example: ensure the ID is not a URL and does not contain forbidden characters.
        return !id.contains("/") && !id.contains(" ") && !id.startsWith("http://") && !id.startsWith("https://");
    }

    /**
     *Updates the checked in users to the firebase for future display.
     */
    public void updateUserIDCheck() {
        DocumentReference checkEventdocRef = db.collection("user").document(userID);


        checkEventdocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Check if checkedEvent field exists
                    if (document.contains("checkedEvent")) {
                        // If it exists, update the array by adding docID
                        checkEventdocRef.update("checkedEvent", FieldValue.arrayUnion(qrContent))
                                .addOnSuccessListener(unused -> Log.d("Firestore", "Document successfully updated!")).addOnFailureListener(e -> Log.w("Firestore", "Error updating document", e));
                    }else {
                        // If it doesn't exist, create a new array with docID
                        checkEventdocRef.update("checkedEvent", FieldValue.arrayUnion(qrContent))
                                .addOnSuccessListener(unused -> Log.d("Firestore", "New organizedEvent field created and document updated!")).addOnFailureListener(e -> Log.w("Firestore", "Error updating document", e));
                    }
                } else {
                    Log.d("Firestore", "No such document");
                }
            } else {
                Log.d("Firestore", "get failed with ", task.getException());
            }
        });
    }

    /**
     * Verifies and handles promo codes and querying the Firestore
     * database for a matching event document.
     */
    public void checkPromoCode(final PromoCodeCheckListener listener){
        String reverseQRString = Helpers.reverseString(qrContent);
        DocumentReference docRef = db.collection("event").document(reverseQRString);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Navigate to EventPage
                    Log.d("Firestore", "PromoCode Scanned and found document with EventID: " + reverseQRString);
                    listener.onPromoCodeChecked(true);
                } else { // QR Code could be a Promo code
                    Log.d("Firestore", "No such document");
                    Toast.makeText(QRScannerActivity.this, "QR Code Not Found", Toast.LENGTH_SHORT).show();
                    listener.onPromoCodeChecked(false);
                }
            } else {
                Log.d("Firestore", "get failed with ", task.getException());
                listener.onPromoCodeChecked(false);
            }
        });
    }

    /**
     * Increments the sign-up count for a specific event by one whenever a check in is made*
     * @param eventID The unique ID for the events.
     */
    public void signUpCount(String eventID) {
        DocumentReference eventRef = db.collection("event").document(eventID);

        //increments the countSignup field by 1. If the field does not exist, it will be created with the value of 1.
        eventRef.update("countSignup", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.d("DEBUG", "countSignup successfully incremented"))
                .addOnFailureListener(e -> Log.w("DEBUG", "Error incrementing countSignup", e));
    }

    /**
     * Fetches the details of an event from the Firestore database.
     *
     * @param eventID  The unique ID for the events.
     * @param callback The callback to check if the event is received for not.
     */
    public void getEvent(String eventID, EventCallback callback) {
        db.collection("event")
                .document(eventID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = new Event();

                    // Get all event details from db
                    String eventName = documentSnapshot.getString("eventName");
                    String eventDes = documentSnapshot.getString("eventDescription");
                    String startTime = documentSnapshot.getString("startTime");
                    String endTime = documentSnapshot.getString("endTime");
                    String location = documentSnapshot.getString("location");
                    String poster = documentSnapshot.getString("poster");
                    String qr = documentSnapshot.getString("checkinQRCode");
                    String promoqr = documentSnapshot.getString("promoQRCode");

                    // Put into event instance
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

                    // Pass the event object to the callback
                    callback.onEventReceived(event);
                })
                .addOnFailureListener(e -> {
                    // Handle errors here
                });
    }

    /**
     * Interface for receiving an event's
     * information asynchronously after they have been fetched from the Firestore.
     */
    public interface EventCallback {
        void onEventReceived(Event event);
    }

    /**
     * Navigates the user to event whose promo code is scanned.
     *
     * @param event The event which we want to navigate to.
     */
    public void goToEvent(Event event) {


        Intent intent = new Intent(this, ViewEventActivity.class);

        // Pass data to the eventDetail activity
        intent.putExtra("eventName", event.getName()); // get name
        //intent.putExtra("organizerName", clickedEvent.getOrganizerID()); // And a getOrganizerName method
        intent.putExtra("startTime", event.getStartTime());
        intent.putExtra("endTime", event.getEndTime());
        intent.putExtra("eventDes", event.getDescription());
        intent.putExtra("location", event.getLocation());
        intent.putExtra("poster", event.getPoster());
        intent.putExtra("qr", event.getQrCode());
        intent.putExtra("promoqr", event.getPromoQR());
        intent.putExtra("origin", "attendee");

        startActivity(intent);
    }

    /**
     * Fetches the FCM token of the organizer of a particular event and
     * sends a notification to them when a milestone is reached.
     * @param message  The message to include in the notification
     * @param qrContent The event ID to send the notification to only the checked-in users of that event.
     */
    public void getRegisteredTokens(String message, String qrContent) {

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        Data data = new Data(message);

        CollectionReference reusedQRCodeCollection = db.collection("user");

        reusedQRCodeCollection.whereArrayContains("organizedEvent", qrContent)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        for (QueryDocumentSnapshot document1 : task1.getResult()) {
                            String documentId = document1.getId(); // This is the document ID
                            String token = document1.getString("token");
                            //Log.d("TOKEN RECEIVED", token);
                            NotificationSender sender = new NotificationSender(data, token);
                            apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Log.d("no document:", "no document with this eventID");
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                        }
                    } else {
                        Log.d("no document:", "no document with this eventID");
                    }
                });
    }

    /**
     * Checks if the number of attendees who have checked in reaches the milestones
     * set by the event organizer.
     * If a milestone is reached, a notification is sent to the event organizer.
     *
     * @param eventID The ID for the event setting the Milestone.
     */
    public void checkMilestone(String eventID) {
        Log.d("test:", "check mile stone is running");
        DocumentReference docRef = db.collection("event").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Long> currentMilestones = (List<Long>) document.get("milestone");
                    if (currentMilestones == null) {
                        currentMilestones = new ArrayList<>();
                    }
                    Map<String, Long> userIDCheckIn = (Map<String, Long>) document.get("userIDCheckIn");
                    if (userIDCheckIn == null) {
                        // No one has checked in yet
                        return;
                    }

                    // Count the number of unique checked-in attendees
                    int checkedInCount = userIDCheckIn.size();
                    Long checkedInCountLong = Long.valueOf(checkedInCount);

                    // Sort milestones to check in ascending order
                    Collections.sort(currentMilestones);

                    // Check if the current number of checked-in attendees matches any milestones
                    for (Long milestone : currentMilestones) {
                        if (checkedInCountLong.equals(milestone)) {
                            // Milestone reached, notify the organizer
                            String eventName = document.getString("eventName");
                            String Message = "Reached the milestone of " + String.valueOf(milestone) + " attendees at " + eventName;

                            Log.d("send eventID", "token");
                            getRegisteredTokens(Message,eventID);
                            //Toast.makeText(QRScannerActivity.this, "Reached the milestone: " + milestone, Toast.LENGTH_SHORT).show();
                            break; //  you only want to notify once per check
                        }
                    }
                } else {
                    Log.d("Firestore", "No such document");
                }
            } else {
                Log.d("Firestore", "get failed with ", task.getException());
            }
        });
    }
}