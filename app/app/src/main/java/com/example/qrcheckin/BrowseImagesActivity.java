package com.example.qrcheckin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Activity for browsing and managing event posters and profile images as an admin.
 * Allows the admin to view and delete images from all the event.
 */
public class BrowseImagesActivity extends AppCompatActivity {
    // Firebase Firestore instance
    private FirebaseFirestore db;

    // Lists to hold event posters and profile images
    private ArrayList<HashMap> eventPosters;
    private ArrayList<HashMap> profileImages;

    // Adapters for grids
    private ImageAdapter eventPosterAdapter;
    private ImageAdapter profileImageAdapter;

    // Grid views for event posters and profile images
    private GridView eventPosterGridView;
    private GridView profileImageGridView;


    // Lists to hold selected event posters and profile images
    private ArrayList<String> selectedEventPosters = new ArrayList<>();
    private ArrayList<String> selectedProfileImages = new ArrayList<>();

    ImageButton back;


    /**
     * Initializes the activity, and the firestore instances. Also creates the UI components
     * @param savedInstanceState Bundle containing the data when activity starts
     */

    Button browseQRCodes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_images);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize lists and adapters
        eventPosters = new ArrayList<>();
        profileImages = new ArrayList<>();
        eventPosterAdapter = new ImageAdapter(this, eventPosters);
        profileImageAdapter = new ImageAdapter(this, profileImages);

        // Initialize grid views
        eventPosterGridView = findViewById(R.id.event_poster_grid);
        profileImageGridView = findViewById(R.id.profile_image_grid);

        // Initialize buttons
        back = findViewById(R.id.button_backArrow);
        browseQRCodes = findViewById(R.id.button_view_qr_codes);


        // Set adapters for grid views
        eventPosterGridView.setAdapter(eventPosterAdapter);
        profileImageGridView.setAdapter(profileImageAdapter);

        // Fetch event posters and profile images from Firestore
        fetchEventPosters();
        fetchProfileImages();

        // Setup click listeners for grid views and delete button
        setupClickListeners();

    }

    /**
     * Fetches event posters from Firestore and updates the view.
     */
    private void fetchEventPosters() {
        db.collection("event").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String eventPoster = document.getString("poster");
                        String userID = document.getId();

                        // Create a HashMap to store image and user ID
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("image", eventPoster);
                        hashMap.put("userID", userID);

                        // Add to event posters list if not null or empty
                        if (eventPoster != null && !eventPoster.isEmpty()) {
                            eventPosters.add(hashMap);
                        }
                    }
                    // Notify adapter after data is fetched
                    eventPosterAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Fetches profile images from Firestore and updates the view.
     */
    private void fetchProfileImages() {
        db.collection("user").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String profileImage = document.getString("profileImage");
                        Boolean isImageSet = document.getBoolean("imageSet");
                        String userID = document.getId();

                        // Create a HashMap to store image and user ID
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("image", profileImage);
                        hashMap.put("userID", userID);

                        // Add to profile images list if not null or empty
                        if ((profileImage != null) && !profileImage.isEmpty() && Boolean.TRUE.equals(isImageSet)) {
                            profileImages.add(hashMap);
                        }
                    }
                    // Notify adapter after data is fetched
                    profileImageAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Sets up click listeners for views and the back button.
     */
    private void setupClickListeners() {
        // Click listener for event poster grid view
        eventPosterGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userId = eventPosters.get(position).get("userID").toString();
                // Display a dialog to confirm deletion
                showDeleteConfirmationDialog(userId, "event");
            }
        });

        // Click listener for profile image grid view
        profileImageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get user ID of selected item
                String userId = profileImages.get(position).get("userID").toString();

                // Display a dialog to confirm deletion
                showDeleteConfirmationDialog(userId, "user");

            }
        });

        back.setOnClickListener(v -> {

            Intent intent = new Intent(BrowseImagesActivity.this, HomepageAdmin.class);
            startActivity(intent);
        });

        browseQRCodes.setOnClickListener(v -> {

            Intent intent = new Intent(BrowseImagesActivity.this, BrowseQRCodeActivity.class);
            startActivity(intent);
        });


    }

    /**
     * Deletes an image from the firestore collection collection.
     * @param userId The ID of the user whose image is deleted.
     * @param collection The collection in Firestore where the image is to be deleted.
     */
    private void deleteImage(String userId, String collection) {
        String field = null;
        Log.d("USERNAME DELETE", collection);
        final String[] fieldValue = new String[1];
        // Determine which field to update based on the collection
        if (collection.equals("event")) {
            //if (collection == "event") {
            field = "poster";
            fieldValue[0] = null;
            updateFirestore(userId, collection, field, fieldValue[0]);

        } else if (collection.equals("user")) {
            field = "profileImage";
            String finalField = field;
            getDefaultProfileImage(userId, new ProfileImageCallback() {
                @Override
                public void onImageGenerated(String base64Image) {
                    fieldValue[0] = base64Image;
                    updateFirestore(userId, collection, finalField, fieldValue[0]);
                }
            });
        }
    }


    /**
     * Displays a confirmation box before deleting an image.
     * @param userId The ID of the user whose image will be deleted.
     * @param collection The collection in Firestore where the image is to be deleted.
     */

    private void updateFirestore(String userId, String collection, String field, String value) {
        db.collection(collection)
                .document(userId)
                .update(field, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Document updated successfully");
                        // Remove deleted image from the corresponding list and notify adapter
                        if (collection.equals("user")) {
                            for (HashMap<String, String> map : profileImages) {
                                if (map.get("userID").equals(userId)) {
                                    profileImages.remove(map);
                                    break;
                                }
                            }
                            profileImageAdapter.notifyDataSetChanged();
                            db.collection(collection).document(userId).update("imageSet", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("Firestore", "Success");
                                    Intent intent = new Intent(BrowseImagesActivity.this, BrowseImagesActivity.class);
                                    startActivity(intent);
                                }
                            });
//                            Intent intent = new Intent(BrowseImagesActivity.this, BrowseImagesActivity.class);
//                            startActivity(intent);
                        } else if (collection.equals("event")) {
                            for (HashMap<String, String> map : eventPosters) {
                                if (map.get("userID").equals(userId)) {
                                    eventPosters.remove(map);
                                    break;
                                }
                            }
                            eventPosterAdapter.notifyDataSetChanged();
                            //Intent intent = new Intent(BrowseImagesActivity.this, BrowseImagesActivity.class);
                            //startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error updating document", e);
                        // Show error message if deletion fails
                        Toast.makeText(BrowseImagesActivity.this, "Failed to delete image. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    interface ProfileImageCallback {
        void onImageGenerated(String base64Image);
    }

    private void getDefaultProfileImage(String userId, ProfileImageCallback callback) {
        final String[] userName = new String[1];

        db.collection("user").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName[0] = documentSnapshot.getString("name");
                String initials = ProfileImageGenerator.getInitials(userName[0]);
                Bitmap imageBitmap = ProfileImageGenerator.generateInitialsImage(initials);
                String imageBase64 = Helpers.bitmapToBase64(imageBitmap);
                callback.onImageGenerated(imageBase64);
            } else {
                Log.e("ProfileActivity", "No such document");
            }
        }).addOnFailureListener(error -> {
            Log.e("ProfileActivity", "Error fetching document", error);
        });
    }

    private void showDeleteConfirmationDialog(final String userId, final String collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the Delete button, delete the image
                        deleteImage(userId, collection);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the Cancel button, dismiss the dialog
                        dialog.dismiss();
                    }
                });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}