package com.example.qrcheckin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
 * An activity that displays the QR codes fetched from Firestore.
 * It allows the admins to browse and delete QR codes of the events.
 */
public class BrowseQRCodeActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ArrayList<HashMap> QRCodes;
    private ImageAdapter QRCodesAdapter;
    private GridView QRCodesGridView;
    private ArrayList<String> selectedQRCodes = new ArrayList<>();

    ImageButton back;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_qrcodes);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Initialize list and adapter for QR codes
        QRCodes = new ArrayList<>();
        QRCodesAdapter = new ImageAdapter(this, QRCodes);

        // Initialize grid view
        QRCodesGridView = findViewById(R.id.qrcode_grid);

        // Set adapter for grid view
        QRCodesGridView.setAdapter(QRCodesAdapter);

        // Initialize back button
        back = findViewById(R.id.button_backArrow);



        // Fetch QR codes from Firestore and set up click listeners
        fetchQRCodes();
        setupClickListeners();
    }


    /**
     * Fetches QR codes from Firestore of the events.
     */
    private void fetchQRCodes() {
        db.collection("event").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String QRCode = document.getString("checkinQRCode");
                        String eventID = document.getId();

                        // Create HashMap to store QR code and user ID
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("image", QRCode);
                        hashMap.put("eventID", eventID);

                        // Add QR code to list if not null or empty
                        if (QRCode != null && !QRCode.isEmpty()) {
                            QRCodes.add(hashMap);
                        }
                    }
                    // Notify adapter after data is fetched
                    QRCodesAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Sets up click listeners for the qr codes in the gridview
     * Also allows the user to delete the selected qr code.
     */
    private void setupClickListeners() {
        QRCodesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userId = QRCodes.get(position).get("eventID").toString();
                showDeleteConfirmationDialog(userId);
            }
        });

        back.setOnClickListener(v -> {

            Intent intent = new Intent(BrowseQRCodeActivity.this, BrowseImagesActivity.class);
            startActivity(intent);
        });

    }

    /**
     * Deletes a QR code from Firestore.
     * @param eventID The ID of the event whose qr code will be deleted.
     * @param collection The name of the Firestore collection where qr code is stored.
     */
    private void deleteImage(final String eventID, String collection) {
        db.collection(collection)
                .document(eventID)
                .update("checkinQRCode", null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Document updated successfully");
                        // Remove deleted QR code from the list and notify adapter
                        for (HashMap<String, String> map : QRCodes) {
                            if (map.get("eventID").equals(eventID)) {
                                QRCodes.remove(map);
                                break;
                            }
                        }
                        QRCodesAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error updating document", e);
                        // Show error message if deletion fails
                        Toast.makeText(BrowseQRCodeActivity.this, "Failed to delete QR code. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Displays a confirmation message to the user before deletion
     * @param userId The ID whose QR code will be deleted.
     */
    private void showDeleteConfirmationDialog(final String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this QR code?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the Delete button, delete the QR code
                        deleteImage(userId, "event");
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