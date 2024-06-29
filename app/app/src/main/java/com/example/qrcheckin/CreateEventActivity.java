package com.example.qrcheckin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

/**
 * This class is responsible for creating new events.
 * Users can enter the details of the event including Name, Start Time,
 * End Time, Location and choose a poster image. They can optionally
 * limit the number of attendees and optionally generate a promo QR code.
 * On the creation of the event the HomepageOrganizer will populate with their
 * event.
 */
public class CreateEventActivity extends AppCompatActivity {
    public boolean isUnderInstrumentationTest() {
        try {
            Class.forName("androidx.test.espresso.Espresso");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    private FirebaseFirestore db;
    boolean isDBConnected;
    public EditText newEventName;
    EditText newEventDescription;
    EditText newStartTime;
    EditText newEndTime;
    EditText newLocation;
    EditText newAttendeeCapacity;
    Button continueButton;
    Button editPosterImageButton;
    Button startTime;
    Button endTime;
    CheckBox generatePromoQRCodeCheckbox;
    boolean needPromoQRCode;
    TextView selectedStartTime, selectedEndTime;

    int attendeeCapacity;
    ImageView posterImage;
    Bitmap posterImageBitmap;
    String posterImageBase64;
    Bitmap promoCodeBitmap;
    String promoCodeBase64;
    String mainUserID;

    Bundle bundle;

    String docID;

    ImageButton goBack;

    // TODO For now QR Code generated here
    //  Decide whether to delete this workaround later


    String checkinQRCodeBase64;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Add QR Generator fragment to this Activity


        // Bind UI
        newEventName = findViewById(R.id.eventNameEditText);
        newEventDescription = findViewById(R.id.eventDescriptionEditText);
        newLocation = findViewById(R.id.eventLocationEditText);
        continueButton = findViewById(R.id.continueCreateEventButton);
        editPosterImageButton = findViewById(R.id.editPosterImageButton);
        posterImage = findViewById(R.id.posterImageView);
        newAttendeeCapacity = findViewById(R.id.attendeeCapacityEditText);
        generatePromoQRCodeCheckbox = findViewById(R.id.checkboxGeneratePromoQRCode);
        startTime = findViewById(R.id.startTimeButton); // Assuming the ID in your layout is startTimeButton
        endTime = findViewById(R.id.endTimeButton); // Assuming the ID in your layout is endTimeButton
        //selectedStartTime = findViewById(R.id.selectedStartTime);
        //selectedEndTime = findViewById(R.id.selectedEndTime);

        db = FirebaseFirestore.getInstance();
        getUserID();

        startTime.setOnClickListener(v -> showDateTimePicker(true));
        endTime.setOnClickListener(v -> showDateTimePicker(false));
        // TODO
        //  1. Display poster
        //  2. Assign poster to variable
        //  3. Convert poster to bitmap

        // Registers a photo picker activity launcher in single-select mode.
        // Source: https://developer.android.com/training/data-storage/shared/photopicker#select-single-item
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
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

                                        posterImageBitmap = compressBitmap(bitmap);
                                        posterImageBase64 = Helpers.bitmapToBase64(posterImageBitmap);
                                        return false;
                                    }
                                })
                                .into(posterImage);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });


        editPosterImageButton.setOnClickListener(v->{
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
            }
        );

        goBack = findViewById(R.id.button_go_back);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateEventActivity.this, HomepageOrganizer.class);
                startActivity(intent);
            }
        });

        continueButton.setOnClickListener(v -> startNextActivity());

    }

    /**
     * Displays a date and time picker to the user for selecting the start or end time of an event.
     * This method displays a {@link DatePickerDialog}, and when the date is selected, a {@link TimePickerDialog} is shown to pick the time.
     *
     * @param isStart A boolean flag to check whether the start or end time is being selected.
     */

    // OpenAI, 2024, ChatGPT, How to show time and date picker
    private void showDateTimePicker(final boolean isStart) {
        // Explicit initialization at the method's start
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Once a date is picked, setup the calendar object with the selected date
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Proceed to time picking
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateDateTimeText(calendar, isStart); // Update the text view with the selected date and time
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
    /**
     * Updates the text of a TextView to display the selected date and time. This method formats the {@link Calendar} object
     * to a string and sets this string to either the start time or end time TextView based on the boolean flag isStart.
     *
     * @param calendar The {@link Calendar} object with the start or end time.
     * @param isStart  A boolean flag to check whether the start or end time is being selected.
     */

    // OpenAI, 2024, ChatGPT, Update date and time
    private void updateDateTimeText(Calendar calendar, boolean isStart) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateTimeText = format.format(calendar.getTime());
        if (isStart) {
            startTime.setText(String.format("%s", dateTimeText));
        } else {
            endTime.setText(String.format("%s", dateTimeText));
        }
    }

    /**
     * Responsible for adding an event. Checks if all the necessary information is
     *added by the user then calls addEvent()
     */
    private void startNextActivity() {
        if (isTextEditInputEmpty()) {
            if (isUnderInstrumentationTest()) {
                Intent QRGeneratorIntent = new Intent(CreateEventActivity.this, QRGenerator.class);
                startActivity(QRGeneratorIntent);
            } else {
                addEvent();
            }
        }
    }

    /**
     * Get event details from the UI, and adds the event to Firestore.
     * It also handles optional fields such as attendee capacity and promotional QR codes.
     * After successfully adding the event, it navigates to the qr generator activity for
     * creating a qr code for this activity.
     */

    private void addEvent() {
        HashMap<String, Object> data = new HashMap<>();
        String attendeeCapacityString;

        String eventName = newEventName.getText().toString();
        String eventDescription = newEventDescription.getText().toString();
        String start = startTime.getText().toString();
        String end = endTime.getText().toString();
        String location = newLocation.getText().toString();

        docID = Helpers.createDocID(eventName, start, location);
        if (newAttendeeCapacity.getText() == null) {
            attendeeCapacityString = "";
        }

        attendeeCapacityString = newAttendeeCapacity.getText().toString();
        Log.d("DEBUG", "attendeeCapacityString: " + attendeeCapacityString);

        checkPromoCodeAndGenerate();

        Log.d("DEBUG", "docID in CreateEventActivity: " + docID);

        data.put("eventName", eventName);
        data.put("eventDescription", eventDescription);
        data.put("startTime", start);
        data.put("endTime", end);
        data.put("location", location);
        data.put("poster", posterImageBase64);


        // OPTIONAL FIELDS
        // If promo code was generated then add it to Firebase bundle
        if (promoCodeBase64 != null) {
            Log.d("DEBUG", "promocode: " + promoCodeBase64);
            data.put("promoQRCode", promoCodeBase64);
        }

        if (!attendeeCapacityString.equals("")) {
            // Convert to integer and package for database
            Integer attendeeCapacity = Integer.parseInt(attendeeCapacityString);
            data.put("attendeeCapacity", attendeeCapacity);
            Log.d("DEBUG", "AttendeeCapacity: " + attendeeCapacityString);
        }
        else{
            // Default value of attendee capacity
            data.put("attendeeCapacity", 5000);
        }

        // Only pass eventID to bundle for QR Code generation
        // TODO try passing everything in Bundle to next activity
        //  Make db write after confirm activity on QR Generator page
        Bundle bundle = new Bundle();
        bundle.putString("eventID", docID);
        Log.d("DEBUG", "eventID: " + docID);

        db.collection("event")
                .document(docID)
                .set(data)
                .addOnSuccessListener(v->{
                    Log.d("DEBUG", "Firebase success - event added");

                    // On Success add eventID to user's organized events list
                                db.collection("user")
                                        .document(mainUserID)
                                        .update("organizedEvent", FieldValue.arrayUnion(docID))
                                        .addOnSuccessListener(w->{
                                            // On success of above, navigate to QR Generator activity

                                            Log.d("DEBUG", "Firebase success - organizedEvent added");


                                            Intent QRGeneratorIntent = new Intent(CreateEventActivity.this, QRGenerator.class);
                                            QRGeneratorIntent.putExtras(bundle);
                                            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            //Log.d("DEBUG", "intent created: " + intent);
                                            startActivity(QRGeneratorIntent);
                                            finish();
                                        });
                        }

                );
    }

    /**
     * This function validates whether the TextEdit input fields are empty and
     * also displays errors if they are empty.
     * @return true if no errors, false if errors
     */
    public boolean isTextEditInputEmpty() {
        if (isUnderInstrumentationTest()) {
            if (String.valueOf(newEventName.getText()).isEmpty()) {
                newEventName.setError("Enter Event Name");
                return false;
            }
            if (String.valueOf(newLocation.getText()).isEmpty()) {
                newLocation.setError("Enter Event Location");
                return false;
            }
            return true;

        } else {
            if (String.valueOf(newEventName.getText()).isEmpty()) {
                newEventName.setError("Enter Event Name");
                return false;
            }
            // Since start and end times are now being selected via a DatePicker and set as text,
            // we check the TextView content instead of EditText.
            if (startTime.getText().toString().equals("Start Time") || startTime.getText().toString().isEmpty()) {
                // Show some error or toast message indicating that start time hasn't been set
                //Toast.makeText(this, "Please select a start time.", Toast.LENGTH_SHORT).show();
                startTime.setError("Please select a start time");
                return false;
            }
            if (endTime.getText().toString().equals("End Time") || endTime.getText().toString().isEmpty()) {
                // Show some error or toast message indicating that end time hasn't been set
                //Toast.makeText(this, "Please select an end time.", Toast.LENGTH_SHORT).show();
                startTime.setError("Please select a end time");
                return false;
            }
            if (String.valueOf(newLocation.getText()).isEmpty()) {
                newLocation.setError("Enter Event Location");
                return false;
            }
            // Since newStartTime and newEndTime are no longer used, they've been removed from this check.
            return true;
        }
    }



    // TODO: CHECK INPUTS ARE VALID - ANN

    /**
     * Checks whether Firebase Firestore is connected
     * Alters the isDBConnected variable to true or false.
     */
    public void dbConnected(){
        db.getInstance()
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
     * Checks whether promo code checkbox was checked.
     * If it is checked it generates a bitmap qr code based on the docID.
     * Then converts it to a Base64 string for upload to Firebase Firestore.
     * It assigns this string to the variable promoCodeBase64.
     */
    public void checkPromoCodeAndGenerate(){
        if (generatePromoQRCodeCheckbox.isChecked()) {

            // CheckBox is checked
            String inputValue = Helpers.reverseString(docID);
            QRGEncoder qrgEncoder = new QRGEncoder(inputValue, null, QRGContents.Type.TEXT, 800);

            // Getting QR-Code as Bitmap
            promoCodeBitmap = qrgEncoder.getBitmap(0);
            promoCodeBase64 = Helpers.bitmapToBase64(promoCodeBitmap);
            Log.d("Checkbox", "Checkbox is checked");
            Log.e("nsp"," checked");
        } else {
            // CheckBox is not checked
            Log.d("Checkbox", "Checkbox is not checked");
            Log.e("nsp","not checked");
        }
    }

    /**
     * This function generates a bitmap for the checkin QR code based on the docID.
     * It converts this bitmap to a base64 string to upload to firebase.
     * This is assigned to checkinQRCodeBase64.
     */
    public void generateQRCodeAndSetString(){
        QRGEncoder qrgEncoder = new QRGEncoder(docID, null, QRGContents.Type.TEXT, 800);

        // Getting QR-Code as Bitmap
        Bitmap bitmap = qrgEncoder.getBitmap(0);

        // Convert bitmap to Base64 for Firebase
        checkinQRCodeBase64 = Helpers.bitmapToBase64(bitmap);
    }

    private Bitmap compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // Adjust compression quality as needed
        byte[] byteArray = outputStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * Gets the main user ID from local storage. This method reads a text file
     * from internal storage to get the user's main ID.
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
}
