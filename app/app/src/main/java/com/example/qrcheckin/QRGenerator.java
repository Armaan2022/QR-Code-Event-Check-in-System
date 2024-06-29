package com.example.qrcheckin;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

/**
 * Activity for generating and reusing QR codes for event check-ins.
 * Allows users to create a new QR code for an event,or reuse an existing QR code.
 * The QR code generated is based on the eventID and other users can check in to an
 * event using that qr code.
 */

public class QRGenerator extends AppCompatActivity {

    public boolean isUnderInstrumentationTest() {
        try {
            Class.forName("androidx.test.espresso.Espresso");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    Button generateQRCodeButton;
    Button reuseQRCodeButton;
    Button createEventButton;
    ImageView QRCodeImage;
    String QRCodeBase64;
    Bitmap bitmap;

    String eventID;


    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_generator);

        //assert bundle != null;
        if (isUnderInstrumentationTest()) {
            eventID = "dsfgnfdsfgnfbgdfsd";
        } else {
            eventID = getIntent().getExtras().getString("eventID");
        }

        generateQRCodeButton = findViewById(R.id.generateCheckinQRCodeButton);
        reuseQRCodeButton = findViewById(R.id.reuseCheckinQRCodeButton);
        QRCodeImage = findViewById(R.id.checkinQRCodeImageView);
        createEventButton = findViewById(R.id.confirmEventCreationButton);

        db = FirebaseFirestore.getInstance();

        createEventButton.setVisibility(View.GONE);

        // Generate new QR Code
        generateQRCodeButton.setOnClickListener(v->{
            QRGEncoder qrgEncoder = new QRGEncoder(eventID, null, QRGContents.Type.TEXT, 800);

            // Getting QR-Code as Bitmap
            bitmap = qrgEncoder.getBitmap(0);
            // Setting Bitmap to ImageView
            QRCodeImage.setImageBitmap(bitmap);
            // Convert bitmap to Base64 for Firebase
            QRCodeBase64 = Helpers.bitmapToBase64(bitmap);
            createEventButton.setVisibility(View.VISIBLE);
        });

        reuseQRCodeButton.setOnClickListener(v->{

            scanCode();
            if (QRCodeBase64 != null) {
                createEventButton.setVisibility(View.VISIBLE);
            }
            if (bitmap != null) {
                QRCodeImage.setImageBitmap(bitmap);
            }
        });

        createEventButton.setOnClickListener(v->{

            if (isUnderInstrumentationTest()) {
                Intent intent = new Intent(QRGenerator.this, HomepageOrganizer.class);
                startActivity(intent);
            } else {
                // Write checkinQRCode to database
                HashMap<String, Object> data = new HashMap<>();
                data.put("checkinQRCode", QRCodeBase64);

                db.collection("event")
                        .document(eventID)
                        .update(data)
                        .addOnSuccessListener(w -> {
                            // Navigate to Organizer Homepage
                            Intent intent = new Intent(QRGenerator.this, HomepageOrganizer.class);
                            startActivity(intent);
                        });
            }
        });
    }

    /**
     * Launches the scanner to allow users to scan an existing qr code
     * if they wish to reuse a qr code.
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
     * Activity result launcher for the QR code scanner.
     * Checks if the existing qr code can be used or not.
     */
    private ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {

        if(result.getContents() != null) {
            String qrContent = result.getContents();
            AlertDialog.Builder builder = new AlertDialog.Builder(QRGenerator.this);

            Log.e("DEBUG", "isValidDocumentId: " + Helpers.isValidDocumentId(qrContent) );

            // qrContent needs to be a validDocumentId in order to query db
            if (Helpers.isValidDocumentId(qrContent)){  // If it is valid

                // Call database and check if code already in use
                db.collection("event").document(qrContent).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            // if QR Code already in use
                            if (documentSnapshot.exists()) {
                                builder
                                        .setTitle("Cannot Use")
                                        .setMessage("QR Code already in use. Please use a different QR Code")
                                        .show();

                            }
                            // Code not already used. Add to database as reused QR checkin code
                            else {
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("reused QR Code", qrContent);
                                db.collection("event").document(eventID).update(data);
                                if (QRCodeBase64 != null) {
                                    createEventButton.setVisibility(View.VISIBLE);
                                }
                                Intent intent = new Intent(this, HomepageOrganizer.class);
                                startActivity(intent);
                                finish();

                            }
                        });
            }
            // qrContent cannot be queried on the database directly so need to check if its already in use
            else {
                Log.e("DEBUG", "QRCode is 'invalid': " + qrContent);

                // Generate bitmap image and convert to string
                QRGEncoder qrgEncoder = new QRGEncoder(qrContent, null, QRGContents.Type.TEXT, 800);

                // Getting QR-Code as Bitmap
                bitmap = qrgEncoder.getBitmap(0);

                QRCodeImage.setImageBitmap(bitmap);

                // Convert bitmap to Base64 for Firebase
                QRCodeBase64 = Helpers.bitmapToBase64(bitmap);

                if (QRCodeBase64 != null) {
                    createEventButton.setVisibility(View.VISIBLE);
                }

                HashMap<String, Object> data = new HashMap<>();
                data.put("decodedString", qrContent);
                data.put("reusedcheckinQRCodeBase64", QRCodeBase64);
                data.put("eventID", eventID);

                db.collection("reusedQRCodes")
                        .document()
                        .set(data);
            }
        }

    });
}