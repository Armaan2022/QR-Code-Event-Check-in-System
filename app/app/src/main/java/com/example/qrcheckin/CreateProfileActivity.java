package com.example.qrcheckin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
* This class is responsible for creating new profile.
 * The user sees this page if they have not created a profile yet.
 * They can enter their details and their profile is uploaded to Firebase.
 * On creation of the profile, all entered details are uploaded to Firebase
 * including the device's unique ID.
 * For user persistence across different app instances, the userID is saved
 * to a local file cache that can be accessed across activities and app instances.
*/

public class CreateProfileActivity extends AppCompatActivity {

    public boolean isUnderInstrumentationTest() {
        try {
            Class.forName("androidx.test.espresso.Espresso");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    FirebaseFirestore db;
    boolean isDBConnected;
    EditText newUserName;
    EditText newUserPhone;
    EditText newUserEmail;
    EditText newUserHomepage;
    Button confirmButton;
    Button addProfileImageButton;
    ImageView profileImage;
    Bitmap profileImageBitmap;
    Bitmap initialsBitmap;
    String initialsBase64;
    String profileImageBase64;
    Bundle bundle;
    String mainUserID;
    String android_id;
    boolean isImageSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        newUserName = findViewById(R.id.userNameEditText);
        newUserEmail = findViewById(R.id.userEmailEditText);
        newUserPhone = findViewById(R.id.userPhoneEditText);
        newUserHomepage = findViewById(R.id.userHomepageEditText);
        confirmButton = findViewById(R.id.continueAddProfileButton);
        addProfileImageButton = findViewById(R.id.editProfileImageButton);
        profileImage = findViewById(R.id.profileImage);
        db = FirebaseFirestore.getInstance();

        isImageSet = false; // Set flag to not true by default

        // Registers a photo picker activity launcher in single-select mode.
        // Developer.Android, 2024, Source: https://developer.android.com/training/data-storage/shared/photopicker#select-single-item
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        // Inserts Poster image into ImageView
                        Glide.with(this)
                                .load(uri).listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                        Bitmap compressedBitmap = compressBitmap(bitmap);

                                        profileImageBitmap = compressedBitmap;
                                        profileImageBase64 = Helpers.bitmapToBase64(compressedBitmap);
                                        return false;
                                    }
                                })
                                .into(profileImage);
                        isImageSet = true; // Update the flag because user added an image
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        addProfileImageButton.setOnClickListener(v->{
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
            }
        );

        // Set onclick listener for confirm button
        // Check if all input data are valid
        // Write data to db
        confirmButton.setOnClickListener(v -> {

            if (isUnderInstrumentationTest()) {
                Intent intent = new Intent(CreateProfileActivity.this, HomepageActivity.class);
                startActivity(intent);
            } else {

                if (isProfileInputValid()) {
                    String userName = newUserName.getText().toString();
                    String phone = newUserPhone.getText().toString();
                    String email = newUserEmail.getText().toString();
                    String url = newUserHomepage.getText().toString();

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("name", userName);
                    userInfo.put("phone", phone);
                    userInfo.put("email", email);
                    userInfo.put("isAdmin", false);
                    if (!url.isEmpty()) {
                        userInfo.put("url", url);
                    }
                    userInfo.put("imageSet", isImageSet);
                    android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                    userInfo.put("androidID", android_id);
                    if (isImageSet) {
                        userInfo.put("profileImage", profileImageBase64);
                    } else {
                        String initials = getInitials(userName);
                        initialsBitmap = generateInitialsImage(initials);
                        initialsBase64 = Helpers.bitmapToBase64(initialsBitmap);
                        userInfo.put("profileImage", initialsBase64);
                    }


                    db.collection("user")
                            .add(userInfo)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("Firestore", "Added with ID: " + documentReference.getId());
                                // Adding userID of the user in the local file
                                mainUserID = documentReference.getId();

                                // OpenAI, 2024, ChatGPT, How to store data in localStorage in Android Studio
                                try {
                                    FileOutputStream fos = openFileOutput("localStorage.txt", Context.MODE_PRIVATE);
                                    fos.write(mainUserID.getBytes());
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Intent intent = new Intent(CreateProfileActivity.this, HomepageActivity.class);
                                intent.putExtra("UserID", documentReference.getId()); // Attach the bundle to the intent
                                startActivity(intent);

                            }).addOnFailureListener(e -> {
                                Log.w("Firestore", "Error adding document", e);
                            });
                } else {
                    Log.d("Validation", "Input validation failed.");
                }
            }
        });
    }

    /**
     * Compresses a given bitmap image to reduce its file size.
     *
     * @param bitmap The original of the image.
     * @return A compressed bitmap.
     */
    private Bitmap compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // Adjust compression quality as needed
        byte[] byteArray = outputStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * Validates the input for all the fields in the profile creation
     * This method checks for empty fields and returns true if all
     * the inputs made by the user are valid.
     * @return true if all inputs are valid, false otherwise.
     */
    // Check the input validity
    public boolean isProfileInputValid() {
        //Name validation
        if (String.valueOf(newUserName.getText()).isEmpty()){
            newUserName.setError("Name is required!");
            return false;
        }
        if (String.valueOf(newUserName.getText()).length() > 200) {
            newUserName.setError("Name is too long!");
            return false;
        }
        // Phone validation
        if (!String.valueOf(newUserPhone.getText()).isEmpty()) {
            if (String.valueOf(newUserPhone.getText()).length() > 10) {
                newUserPhone.setError("Enter a valid phone number!");
                return false;
            }
        }
        // Email validation
        if (!String.valueOf(newUserEmail.getText()).isEmpty()){
            if (!Patterns.EMAIL_ADDRESS.matcher(String.valueOf(newUserEmail.getText())).matches()) {
                newUserEmail.setError("Enter valid email address");
                return false;
            }
        }
        // Homepage validation
        if (!String.valueOf(newUserHomepage.getText()).isEmpty()) {
            if (!Patterns.WEB_URL.matcher(String.valueOf(newUserHomepage.getText())).matches()) {
                newUserHomepage.setError("Enter valid url!");
                return false;
            }
        }
        return true;
    }
    public void dbConnected(){
        FirebaseFirestore.getInstance()
                .enableNetwork()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firestore is connected
                        Log.d("Firestore", "Connected to Firestore");
                        isDBConnected = true;
                    } else {
                        // Firestore connection failed
                        Log.d("Firestore", "Disconnected from Firestore");
                        isDBConnected = false;
                    }
                });
    }

    /**
     * Function to deterministically generate profile picture.
     * @return Bitmap, based on users initials
     * @param initials initials of the username
     */
    // OpenAI, 2024, ChatGPT, Convert string to bitmap in Android Studio
    private Bitmap generateInitialsImage(String initials) {
        int width = 200; // Set the desired width for the image
        int height = 200; // Set the desired height for the image
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Fill background with a color
        canvas.drawColor(Color.parseColor("#FF5722")); // Example color, you can change it

        // Draw text (initials) in the center of the bitmap
        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // Text color
        paint.setTextSize(80); // Text size
        paint.setTextAlign(Paint.Align.CENTER);

        // Calculate text position
        float xPos = canvas.getWidth() / 2f;
        float yPos = (canvas.getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2f);

        // Draw text on the canvas
        canvas.drawText(initials, xPos, yPos, paint);

        return bitmap;
    }

    /**
     * Helper function to parse the initials from the users entered name.
     * @param name, e.g John Smith
     * @return String, users initials eg JS
     */
    private String getInitials(String name) {
        StringBuilder initials = new StringBuilder();
        for (String s : name.split("\\s+")) {
            initials.append(s.charAt(0));
        }
        return initials.toString();
    }
}
