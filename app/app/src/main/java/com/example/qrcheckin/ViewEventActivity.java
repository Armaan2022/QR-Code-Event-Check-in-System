package com.example.qrcheckin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.qrcheckin.SendNotificationPack.APIService;
import com.example.qrcheckin.SendNotificationPack.Client;
import com.example.qrcheckin.SendNotificationPack.Data;
import com.example.qrcheckin.SendNotificationPack.MyResponse;
import com.example.qrcheckin.SendNotificationPack.NotificationSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/** ViewEventActivity allows users to view detailed information about an event.
 * Users can view event details, sign up for events, and view announcements related to the event.
 * This activity interacts with Firebase Firestore to retrieve and update event and user information.
 * @see AddAnnouncementFragment creates a fragment for the announcement
 */

public class ViewEventActivity extends AppCompatActivity implements AddAnnouncementFragment.AddAnnouncementDialogListener,MilestoneFragment.AddMilestoneDialogListener {

    ImageView posterImage;
    TextView eventName;
    TextView eventDescription;
    TextView eventStartTime;
    TextView eventEndTime;
    TextView eventLocation;
    Button DeletePoster;
    Button DeleteEvent;
    Button viewMapBtn;
    Button setMilestone;
    Button signUpButton;
    ImageButton share;
    ImageButton sharePromo;
    ImageButton addAnnouncement;
    ImageView qrCodeImage;
    TextView promoQRCodeTextViewTitle;
    ImageView promoQRCodeImage;
    LinearLayout promoQRArea;
    private FirebaseFirestore db;
    private ArrayList<Announcement> announcementDataList;
    private ListView announcementList;
    private AnnouncementsAdapter announcementsAdapter;

    private ArrayList<Profile> signedAttendeeDataList;
    private ListView signedAttendeeList;
    private SignedAttendeeAdapter signedAttendeeAdapter;

    private ArrayList<Profile> attendeeDataList;
    private ListView attendeeList;
    private AttendeeAdapter attendeeAdapter;

    private String mainUserID;
    private String eventID;
    private APIService apiService;
    LinearLayout posterArea;

    ImageButton back;

    public boolean isAdmin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        posterImage = findViewById(R.id.posterImageView);
        eventDescription = findViewById(R.id.eventDescriptionText);
        eventStartTime = findViewById(R.id.eventStartText);
        eventEndTime = findViewById(R.id.eventEndText);
        eventLocation = findViewById(R.id.eventLocationText);
        eventName = findViewById(R.id.viewEventTitle);

        signUpButton = findViewById(R.id.signUpButton);
        back = findViewById(R.id.button_back);
        qrCodeImage = findViewById(R.id.qrCodeImageView);
        promoQRCodeImage = findViewById(R.id.promoqrCodeImageView);
        DeleteEvent = findViewById(R.id.EventDeleteButton);
        viewMapBtn = findViewById(R.id.viewMapButton);
        addAnnouncement = findViewById(R.id.button_add_announcement);
        share = findViewById(R.id.button_share);
        promoQRCodeTextViewTitle = findViewById(R.id.promoQRCodeTitle);
        DeletePoster  = findViewById(R.id.DeletePosterButton);
        setMilestone = findViewById(R.id.setMileStoneButton);

        posterArea = findViewById(R.id.posterArea);

        promoQRArea = findViewById(R.id.promoQRArea);
        sharePromo = findViewById(R.id.promo_button_share);

        setMilestone = findViewById(R.id.setMileStoneButton);

        Intent intent = getIntent();
        String name = intent.getStringExtra("eventName");
        String eventDes = intent.getStringExtra("eventDes");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");
        String location = intent.getStringExtra("location");
        String qr = intent.getStringExtra("qr");
        String promoqr = intent.getStringExtra("promoqr");
        String poster = intent.getStringExtra("poster");
        String EventID = intent.getStringExtra("EventID");

        // Rehash the relevant fields to get eventID
        eventID = Helpers.createDocID(name, startTime, location);
        Log.d("DEBUG", "eventID " + eventID);

        // Get Firebase
        db = FirebaseFirestore.getInstance();

        eventDescription.setText(eventDes);
        eventStartTime.setText(startTime);
        eventEndTime.setText(endTime);
        eventLocation.setText(location);
        eventName.setText(name);

        if (poster!=null){
            Bitmap posterBitmap = Helpers.base64ToBitmap(poster);
            posterImage.setImageBitmap(posterBitmap);
        } else {
            posterArea.setVisibility(View.GONE);
        }

        if (qr!=null){
            Bitmap qrBitmap = Helpers.base64ToBitmap(qr);
            qrCodeImage.setImageBitmap(qrBitmap);
        }

        if (promoqr!=null){
            Bitmap promoqrBitmap = Helpers.base64ToBitmap(promoqr);
            promoQRCodeImage.setImageBitmap(promoqrBitmap);
        } else {
            promoQRCodeTextViewTitle.setVisibility(View.GONE);
            promoQRArea.setVisibility(View.GONE);
        }

        isAdmin = isAdmin();

        if (isAdmin){
            viewMapBtn.setVisibility(View.GONE);

            setMilestone.setVisibility(View.GONE);

            //DeletePoster.setVisibility(View.VISIBLE);
            DeleteEvent.setVisibility(View.VISIBLE);
            DeletePoster.setVisibility(View.GONE);
            addAnnouncement.setVisibility(View.GONE);

        }

        if (isAttendee()) {
            ConstraintLayout eventButtons = findViewById(R.id.eventButtons);
            LinearLayout attendeeInfo = findViewById(R.id.attendeesInfo);
            LinearLayout signedAttendeeInfo = findViewById(R.id.signedAttendeesInfo);
            ConstraintLayout signUpButton = findViewById(R.id.signInButtonArea);
            signUpButton.setVisibility(View.VISIBLE);
            eventButtons.setVisibility(View.GONE);
            attendeeInfo.setVisibility(View.GONE);
            signedAttendeeInfo.setVisibility(View.GONE);
            DeletePoster.setVisibility(View.GONE);
            addAnnouncement.setVisibility(View.GONE);
            share.setVisibility(View.GONE);
            promoQRCodeTextViewTitle.setVisibility(View.GONE);
            promoQRArea.setVisibility(View.GONE);
            promoQRCodeImage.setVisibility(View.GONE);
            DeleteEvent.setVisibility(View.GONE);
        }

        if (isSignedAttendee()) {
            ConstraintLayout eventButtons = findViewById(R.id.eventButtons);
            LinearLayout attendeeInfo = findViewById(R.id.attendeesInfo);
            LinearLayout signedAttendeeInfo = findViewById(R.id.signedAttendeesInfo);
            ConstraintLayout signUpButton = findViewById(R.id.signInButtonArea);
            signUpButton.setVisibility(View.GONE);
            eventButtons.setVisibility(View.GONE);
            attendeeInfo.setVisibility(View.GONE);
            signedAttendeeInfo.setVisibility(View.GONE);
            DeletePoster.setVisibility(View.GONE);
            addAnnouncement.setVisibility(View.GONE);
            share.setVisibility(View.GONE);
            promoQRCodeTextViewTitle.setVisibility(View.GONE);
            promoQRArea.setVisibility(View.GONE);
            promoQRCodeImage.setVisibility(View.GONE);
            DeleteEvent.setVisibility(View.GONE);
            setMilestone.setVisibility(View.GONE);
        }

        // View map
        viewMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(ViewEventActivity.this, MapActivity.class);
                mapIntent.putExtra("eventName", name); // get name
                //intent.putExtra("organizerName", clickedEvent.getOrganizerID()); // And a getOrganizerName method
                mapIntent.putExtra("startTime", startTime);
                mapIntent.putExtra("endTime", endTime);
                mapIntent.putExtra("eventDes", eventDes);
                mapIntent.putExtra("location", location);
                mapIntent.putExtra("poster",poster);
                mapIntent.putExtra("qr",qr);
                mapIntent.putExtra("promoqr",promoqr);
                mapIntent.putExtra("origin", "organiser");
                mapIntent.putExtra("EVENT_ID", eventID);
                startActivity(mapIntent);
            }
        });


        setMilestone.setOnClickListener(v -> {
            new MilestoneFragment().show(getSupportFragmentManager(), "Add a milestone");
        });


        // OPEN AI, 2024, ChatGPT, Share Images in Android Studio
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) qrCodeImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QR Code", null);
                Uri imageUri = Uri.parse(path);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
            }
        });

        sharePromo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) qrCodeImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Promo QR Code", null);
                Uri imageUri = Uri.parse(path);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share Promo QR Code"));
            }
        });

        getUserID();
        //get the document depending on the eventID
        DocumentReference docRef = db.collection("event").document(eventID);
        //get the users' documents by users' collection
        //DocumentReference userRef = db.collection("users").document(mainUserID);
        //wrapper for storing the attendeeCapacity from firebase

        final int[] attendeeCapacityWrapper = new int[1];
        final int[] attendeeSignUpCount = new int[1];

        // get the attendeeCapacity from firebase
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    // Check if the document contains the attendeeCapacity field
                    if (document.exists() && document.contains("attendeeCapacity")) {
                        // Get the value of attendeeCapacity
                        Number tempCapacity = document.getLong("attendeeCapacity"); // Firestore stores numbers as Long by default
                        // deal with the case where attendeeCapacity does not exist
                        if(tempCapacity != null) {
                            attendeeCapacityWrapper[0] = tempCapacity.intValue();
                            Log.d("Firestore", "Attendee Capacity: " + attendeeCapacityWrapper[0]);
                        }
                    } else {
                        Log.d("Firestore", "No such document or field");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    // Check if the document contains the attendeeCapacity field
                    if (document.exists() && document.contains("countSignup")) {
                        // Get the value of attendeeCapacity
                        Number tempCapacity = document.getLong("countSignup"); // Firestore stores numbers as Long by default
                        // deal with the case where attendeeCapacity does not exist
                        if(tempCapacity != null) {
                            attendeeSignUpCount[0] = tempCapacity.intValue();
                            Log.d("Firestore", "countSignup: " + attendeeSignUpCount[0]);
                        }
                    } else {
                        Log.d("Firestore", "No such document or field");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });
        // Sign up to the event as an attendee
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Reference to the specific event document
                DocumentReference eventRef = db.collection("event").document(eventID);

                eventRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //  list of user IDs who have signed up
                        List<String> signedUpUsers = (List<String>) documentSnapshot.get("signedUpAttendees");
                        List<String> checkInUsers = (List<String>) documentSnapshot.get("UserIDCheckIn");
                        if (signedUpUsers != null && signedUpUsers.contains(mainUserID)) {
                            // User ID is found in the list, indicating they've already signed up
                            Toast.makeText(ViewEventActivity.this, "You have already signed up for this event!", Toast.LENGTH_LONG).show();
                        } else if (checkInUsers != null && checkInUsers.contains(mainUserID)) {
                            Toast.makeText(ViewEventActivity.this, "You have already checked In for this event!", Toast.LENGTH_LONG).show();
                        } else {
                            // User ID is not in the list - proceed with the sign-up process
                            // Ensure there's capacity for more attendees before proceeding
                            //"capacity wrapper" is the max capacity for that event
                            //"SignUpCount" is the total number of attendeeSignedUp
                            if (attendeeSignUpCount[0] < attendeeCapacityWrapper[0]) {
                                signUpAttendee();
                                signUpCount();
                                Intent intent = new Intent(ViewEventActivity.this, SignedUpEventActivity.class);
                                startActivity(intent);
                            } else {
                                // Event is full
                                Toast.makeText(ViewEventActivity.this, "The event is full. You cannot sign up anymore.", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // Handle the case where the event document does not exist
                        Log.d("DEBUG", "Event document does not exist.");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Error", "Failed to fetch event document", e);
                    // handle the failure case
                });
            }
        });

        //x
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAdmin) {
                    Intent intent = new Intent(ViewEventActivity.this, HomepageAdmin.class);
                    startActivity(intent);
                }
                else if (isSignedAttendee()){
                    Intent intent = new Intent(ViewEventActivity.this, SignedUpEventActivity.class);
                    startActivity(intent);
                } else if (isAttendee()) {
                    Intent intent = new Intent(ViewEventActivity.this, HomepageActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(ViewEventActivity.this, HomepageOrganizer.class);
                    startActivity(intent);
                }
            }
        });
        DeleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("event").document(EventID).delete().addOnSuccessListener(w->{
                    Toast.makeText(getApplicationContext(), "Event Deleted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ViewEventActivity.this, HomepageAdmin.class);
                    startActivity(intent);
                });
            }
        });

        announcementDataList = new ArrayList<>();
        announcementList = findViewById(R.id.announcement_list);

        announcementsAdapter = new AnnouncementsAdapter(this, announcementDataList);
        announcementList.setAdapter(announcementsAdapter);

        showAnnouncement();

        // Add announcement
        addAnnouncement.setOnClickListener(v -> {
            new AddAnnouncementFragment().show(getSupportFragmentManager(), "Add an announcement");
        });

        // Create a list for checked-in attendees
        attendeeDataList = new ArrayList<>();
        attendeeList = findViewById(R.id.attendees_list);
        attendeeAdapter = new AttendeeAdapter(this, attendeeDataList, mainUserID,eventID);
        attendeeList.setAdapter(attendeeAdapter);

        showAttendee();

        // Create a list for signed-up attendees
        signedAttendeeDataList = new ArrayList<>();
        signedAttendeeList = findViewById(R.id.signedAttendees_list);
        signedAttendeeAdapter = new SignedAttendeeAdapter(this, signedAttendeeDataList);
        signedAttendeeList.setAdapter(signedAttendeeAdapter);

        showSignedAttendees();
    }

    /**
     * Gets the token of all signed-in attendees of the event and sends them a push notification
     * @param message The message to be delivered to the attendees
     */
    // Developer.Android, 2024, Source: https://developer.android.com/develop/ui/views/notifications/build-notification
    public void getRegisteredTokens(String message) {
        ArrayList<String> registeredTokens = new ArrayList<>();

        // Firebase.google, 2024, source: https://firebase.google.com/docs/cloud-messaging/android/client?_gl=1*1i6arn*_up*MQ..*_ga*MTE5MjUwOTY4NS4xNzEyNTU1Njcz*_ga_CW55HF8NVT*MTcxMjU1NTY3My4xLjAuMTcxMjU1NTcwNS4wLjAuMA..
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        Data data = new Data(message);
        db.collection("event").document(eventID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!= null){
                    Log.e("FirestoreError", "Error getting event details",error);
                    return;
                }
                Log.d("FirestoreSuccess", "Successfully fetched events.");

                if(value.exists()) {
                    ArrayList<String> attendees = (ArrayList<String>) value.get("signedUpAttendees");
                    if (attendees != null && !attendees.isEmpty()){
                        for (String attendeeID : attendees){
                            DocumentReference attendeeRef = db.collection("user").document(attendeeID);
                            attendeeRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot doc) {
                                    if (doc.exists()) {
                                        String attendeeToken = doc.getString("token");
                                        NotificationSender sender = new NotificationSender(data, attendeeToken);

                                        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                                            @Override
                                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                                if (response.code() == 200) {
                                                    if (response.body().success != 1) {
                                                        Toast.makeText(ViewEventActivity.this, "Failed", Toast.LENGTH_LONG);
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onFailure(Call<MyResponse> call, Throwable t) {

                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * Adds the announcement to the event database in the firebase and calls getRegisteredTokens() to send the
     * notification to the attendees;
     * @param announcement The new announcement that was added.
     */
    // OpenAI, 2024, ChatGPT, how to send a notification using FCM
    public void addNotification(Announcement announcement) {
        String message = announcement.getAnnouncement();
        DocumentReference userRef = db.collection("event").document(eventID);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Check if signedUpAttendees field exists
                        if (document.contains("announcements")) {
                            // If it exists, update the array by adding docID
                            userRef.update("announcements", FieldValue.arrayUnion(message))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firestore", "Document successfully updated!");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error updating document", e);
                                        }
                                    });
                        } else {
                            // If it doesn't exist, create a new array with docID
                            userRef.update("announcements", FieldValue.arrayUnion(message))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firestore", "New signedUpAttendees field created and document updated!");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error updating document", e);
                                        }
                                    });
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });

        // Send the message to all attendees as push notification
        getRegisteredTokens(message);

        // Update the announcement listView in Event Details
        showAnnouncement();
    }

    /**
     * Adds the announcement to the event database in the firebase and calls showAnnouncement()
     * @param announcement The new announcement that was added.
     */
    @Override
    public void addAnnouncement(Announcement announcement) {
        String message = announcement.getAnnouncement();

        DocumentReference userRef = db.collection("event").document(eventID);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Check if signedUpAttendees field exists
                        if (document.contains("announcements")) {
                            // If it exists, update the array by adding docID
                            userRef.update("announcements", FieldValue.arrayUnion(message))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firestore", "Document successfully updated!");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error updating document", e);
                                        }
                                    });
                        } else {
                            // If it doesn't exist, create a new array with docID
                            userRef.update("announcements", FieldValue.arrayUnion(message))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firestore", "New signedUpAttendees field created and document updated!");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error updating document", e);
                                        }
                                    });
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });

        showAnnouncement();
    }

    /**
     * Adds a milestone to the firebase of event
     * @param numAttendee The no. of attendees milestone enter by the user
     */
    public void addMilestone(int numAttendee) {
        DocumentReference userRef = db.collection("event").document(eventID);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    //  current milestone array from the document
                    List<Integer> currentMilestones = (List<Integer>) document.get("milestone");
                    if (currentMilestones == null) {
                        currentMilestones = new ArrayList<>();
                    }

                    // Check if numAttendee is already in the milestone array
                    if (!currentMilestones.contains(numAttendee)) {
                        // If not, add numAttendee to the milestone array
                        userRef.update("milestone", FieldValue.arrayUnion(numAttendee))
                                .addOnSuccessListener(unused -> Log.d("Firestore", "Milestone updated successfully!"))
                                .addOnFailureListener(e -> Log.w("Firestore", "Error updating milestone", e));
                    } else {
                        // numAttendee is already in the milestone array; handle as needed
                        Toast.makeText(getApplicationContext(), "You already have this in set", Toast.LENGTH_LONG).show();
                        Log.d("Firestore", "Milestone already contains the attendee number. No update needed.");
                    }
                } else {
                    Log.d("Firestore", "No such document");
                    // Consider creating the document or handling this case as needed
                }
            } else {
                Log.d("Firestore", "get failed with ", task.getException());
                // Consider handling this failure case as needed
            }
        });
    }

    public void checkMilestone() {
        DocumentReference docRef = db.collection("event").document(eventID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Integer> currentMilestones = (List<Integer>) document.get("milestone");
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

                    // Sort milestones to check in ascending order
                    Collections.sort(currentMilestones);

                    // Check if the current number of checked-in attendees matches any milestones
                    for (int milestone : currentMilestones) {
                        if (checkedInCount == milestone) {
                            // Milestone reached, notify the organizer
                            notifyOrganizer(milestone);
                            break; // Assuming you only want to notify once per check
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

    /**
     * Created a message to notify the organiser of the milestone
     * @param milestone The milestone value that ws reached
     */
    private void notifyOrganizer(int milestone) {
        // Implementation of the notification logic
        Log.d("Firestore", "Milestone reached: " + milestone + " attendees have checked in.");
    }
    /**
     * Updates the announcement listView from the firebase and shows on the event details page
     */
    public void showAnnouncement(){
        db.collection("event").document(eventID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!= null){
                    Log.e("FirestoreError", "Error getting event details",error);
                    return;
                }
                Log.d("FirestoreSuccess", "Successfully fetched events.");
                announcementDataList.clear();
                if(value.exists()) {
                    ArrayList<String> announcements = (ArrayList<String>) value.get("announcements");
                    if (announcements != null && !announcements.isEmpty()){
                        for (String announcementMsg : announcements) {
                            Announcement announcement = new Announcement(announcementMsg);
                            announcementDataList.add(0, announcement);
                            announcementsAdapter.notifyDataSetChanged();
                        }

                        // OpenAI, 2024, ChatGPT, How to dynamically increase the size of a list view
                        int totalHeight = 0;
                        for (int i = 0; i < announcementsAdapter.getCount(); i++) {
                            View listItem = announcementsAdapter.getView(i, null, announcementList);
                            listItem.measure(0, 0);
                            totalHeight += listItem.getMeasuredHeight();
                        }

                        // Set the height of the ListView
                        ViewGroup.LayoutParams params = announcementList.getLayoutParams();
                        params.height = totalHeight + (announcementList.getDividerHeight() * (announcementsAdapter.getCount() - 1));
                        announcementList.setLayoutParams(params);
                        announcementList.requestLayout();
                    }
                }
            }
        });
    }

    /**
     * Increment the signUpCount in the firebase by 1
     */
    //reference: StackOverFlow, https://stackoverflow.com/questions/50762923/how-to-increment-existing-number-field-in-cloud-firestore
    public void signUpCount() {
        DocumentReference eventRef = db.collection("event").document(eventID);

        //increments the countSignup field by 1. If the field does not exist, it will be created with the value of 1.
        eventRef.update("countSignup", FieldValue.increment(1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DEBUG", "countSignup successfully incremented");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DEBUG", "Error incrementing countSignup", e);

                    }
                });
    }

    /**
     * Updates the attendee listView from the firebase and shows on the event details page
     */
    public void showAttendee() {
        // Gets all the checked-in attendees from the firebase for an event and displays it
        db.collection("event").document(eventID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!= null){
                    Log.e("FirestoreError", "Error getting event details",error);
                    return;
                }
                Log.d("FirestoreSuccess", "Successfully fetched events.");
                attendeeDataList.clear();
                if(value.exists()) {
                    Map<String, Long> userIDCheckIn = (Map<String, Long>) value.get("userIDCheckIn");
                    if (userIDCheckIn != null && !userIDCheckIn.isEmpty()){
                        for (String attendeeID : userIDCheckIn.keySet()){
                            DocumentReference attendeeRef = db.collection("user").document(attendeeID);
                            attendeeRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot doc) {
                                    if (doc.exists()) {
                                        String attendeeName = doc.getString("name");
                                        String attendeePhone = doc.getString("phone");
                                        String attendeeEmail = doc.getString("email");
                                        String attendeeHomepage = doc.getString("homepage");
                                        String userID = doc.getId();
                                        Long checkInCount = userIDCheckIn.get(userID);

                                        Profile attendee = new Profile(attendeeName, attendeePhone, attendeeEmail, attendeeHomepage);
                                        attendee.setCheckInCount(checkInCount);
                                        attendeeDataList.add(attendee);
                                    }
                                    attendeeAdapter.notifyDataSetChanged();
                                    int totalHeight = 0;
                                    for (int i = 0; i < attendeeAdapter.getCount(); i++) {
                                        View listItem = attendeeAdapter.getView(i, null, attendeeList);
                                        listItem.measure(0, 0);
                                        totalHeight += listItem.getMeasuredHeight();
                                    }

                                    ViewGroup.LayoutParams params = attendeeList.getLayoutParams();
                                    params.height = totalHeight + (attendeeList.getDividerHeight() * (attendeeAdapter.getCount() - 1));
                                    attendeeList.setLayoutParams(params);
                                    attendeeList.requestLayout();
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * Updates the signed-in attendee listView from the firebase and shows on the event details page
     */
    public void showSignedAttendees() {
        // Gets all the signed-up attendees from the firebase for an event and displays it
        db.collection("event").document(eventID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!= null){
                    Log.e("FirestoreError", "Error getting event details",error);
                    return;
                }
                Log.d("FirestoreSuccess", "Successfully fetched events.");
                signedAttendeeDataList.clear();
                if(value.exists()) {
                    ArrayList<String> attendees = (ArrayList<String>) value.get("signedUpAttendees");
                    if (attendees != null && !attendees.isEmpty()){
                        for (String attendeeID : attendees){
                            if (attendeeID != null) {
                                DocumentReference attendeeRef = db.collection("user").document(attendeeID);
                                attendeeRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot doc) {
                                        if (doc.exists()) {
                                            String attendeeName = doc.getString("name");
                                            String attendeePhone = doc.getString("phone");
                                            String attendeeEmail = doc.getString("email");
                                            String attendeeHomepage = doc.getString("homepage");
                                            Profile attendee = new Profile(attendeeName, attendeePhone, attendeeEmail, attendeeHomepage);
                                            signedAttendeeDataList.add(attendee);
                                        }
                                        signedAttendeeAdapter.notifyDataSetChanged();
                                        int totalHeight = 0;
                                        for (int i = 0; i < signedAttendeeAdapter.getCount(); i++) {
                                            View listItem = signedAttendeeAdapter.getView(i, null, signedAttendeeList);
                                            listItem.measure(0, 0);
                                            totalHeight += listItem.getMeasuredHeight();
                                        }
                                        ViewGroup.LayoutParams params = signedAttendeeList.getLayoutParams();
                                        params.height = totalHeight + (signedAttendeeList.getDividerHeight() * (signedAttendeeAdapter.getCount() - 1));
                                        signedAttendeeList.setLayoutParams(params);
                                        signedAttendeeList.requestLayout();
                                    }
                                });
                            }
                            else{
                                Log.d("DEBUG", "User ID not found");
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Checks if the current user is an attendee of the event.
     * @return true if the user is an attendee, false otherwise.
     */
    public boolean isSignedAttendee() {

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle!=null){
            String origin = bundle.getString("origin");
            if(origin!=null && origin.equals("signedinattendee")){
                //from attendee
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current user is a normal user of the event.
     * @return true if the user is in the main homepage, false otherwise.
     */
    public boolean isAttendee() {
        // Checks if the request for this page is coming from an attendee or an organizer
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle!=null){
            String origin = bundle.getString("origin");
            if(origin!=null && origin.equals("attendee")){
                //from attendee
                return true;
            }
        }
        return false;
    }
    /**
     * Checks if the current user is an admin.
     * @return true if the user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        // Checks if the request for this page is coming from an admin
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle!=null){
            String origin = bundle.getString("origin");
            if(origin!=null && origin.equals("admin")){
                //from attendee
                return true;
            }
            else return false;
        }
        return false;
    }
    /**
     * Signs up the current user as an attendee for the event.
     */
    public void signUpAttendee(){

        getUserID();

        DocumentReference eventRef = db.collection("event").document(eventID);
        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Check if signedUpAttendees field exists
                        if (document.contains("signedUpAttendees")) {
                            // If it exists, update the array by adding docID
                            eventRef.update("signedUpAttendees", FieldValue.arrayUnion(mainUserID))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firestore", "Document successfully updated!");
                                            addToSignedUpEventsInProfile();
                                            Toast.makeText(ViewEventActivity.this, "You signed up!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error updating document", e);
                                        }
                                    });
                        } else {
                            // If it doesn't exist, create a new array with docID

                            eventRef.update("signedUpAttendees", FieldValue.arrayUnion(mainUserID))

                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firestore", "New signedUpAttendees field created and document updated!");
                                            addToSignedUpEventsInProfile();
                                            Toast.makeText(ViewEventActivity.this, "You signed up!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error updating document", e);
                                        }
                                    });
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Retrieves the user ID of the current user from local storage.
     */
    public void getUserID(){
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
    }

    /**
     * Adds the current event to the user's list of signed-up events in their profile.
     */
    public void addToSignedUpEventsInProfile(){
        // Update user's document with signed up events
        if (mainUserID != null){
        DocumentReference userRef = db.collection("user").document(mainUserID);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Check if signedUpAttendees field exists
                        if (document.contains("signedUpEvents")) {
                            // If it exists, update the array by adding docID
                            userRef.update("signedUpEvents", FieldValue.arrayUnion(eventID))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firestore", "User document successfully updated!");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error updating User document", e);
                                        }
                                    });
                        } else {
                            // If it doesn't exist, create a new array with docID
                            userRef.update("signedUpEvents", FieldValue.arrayUnion(eventID))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firestore", "New signedUpEvents field created and document updated!");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error updating user document", e);
                                        }
                                    });
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            }
        });
        }
        else{
            Log.d("DEBUG", "User ID not found");
        }
    }
}