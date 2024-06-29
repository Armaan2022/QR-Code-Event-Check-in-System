package com.example.qrcheckin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Map;
/**
 * Activity that displays a map with markers for each user who has checked in to an event.
 * By default the map is set to Edmonton, but markers are placed based on the latitude and longitude
 * values of the check in location.
 */
public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private FirebaseFirestore db;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        back = findViewById(R.id.button_back);

        // Get the intents
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

        // Important: Initialize the osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences(getPackageName(), MODE_PRIVATE));

        mapView = findViewById(R.id.map_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Set initial map view to Edmonton
        GeoPoint edmontonPoint = new GeoPoint(53.5461, -113.4938);
        mapView.getController().setZoom(15);
        mapView.getController().setCenter(edmontonPoint);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, ViewEventActivity.class);
                intent.putExtra("eventName", name); // get name
                //intent.putExtra("organizerName", clickedEvent.getOrganizerID()); // And a getOrganizerName method
                intent.putExtra("startTime", startTime);
                intent.putExtra("endTime", endTime);
                intent.putExtra("eventDes", eventDes);
                intent.putExtra("location", location);
                intent.putExtra("poster",poster);
                intent.putExtra("qr",qr);
                intent.putExtra("promoqr",promoqr);
                intent.putExtra("origin", "organiser");
                startActivity(intent);
            }
        });

        // OpenAI, 2024, ChatGPT, How to show the hashmap values of latitude and longitude on the map
        db = FirebaseFirestore.getInstance();
        String eventID = getIntent().getStringExtra("EVENT_ID");

        if (eventID != null && !eventID.isEmpty()) {
            DocumentReference eventDocRef = db.collection("event").document(eventID);
            eventDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> checkIns = (Map<String, Object>) documentSnapshot.get("checkIns");

                    if (checkIns != null) {
                        for (Map.Entry<String, Object> entry : checkIns.entrySet()) {
                            String userID = entry.getKey();
                            db.collection("user").document(userID).get().addOnSuccessListener(userDocumentSnapshot -> {
                                if (userDocumentSnapshot.exists()) {
                                    String userName = userDocumentSnapshot.getString("name");
                                    Map<String, Object> checkInData = (Map<String, Object>) entry.getValue();
                                    Double latitude = (Double) checkInData.get("latitude");
                                    Double longitude = (Double) checkInData.get("longitude");

                                    runOnUiThread(() -> {
                                        if (latitude != null && longitude != null && userName != null) {
                                            GeoPoint attendeeLocation = new GeoPoint(latitude, longitude);
                                            Marker marker = new Marker(mapView);
                                            marker.setPosition(attendeeLocation);
                                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                            marker.setTitle(userName); // Set the user's name as the marker title
                                            mapView.getOverlays().add(marker);
                                            mapView.invalidate(); // Refresh the map to display the marker
                                        }
                                    });
                                }
                            }).addOnFailureListener(e -> {
                                // Handle the error, e.g., the user document does not exist or failed to retrieve
                            });
                        }
                    }
                }
            }).addOnFailureListener(e -> {
                // Handle the error, e.g., the event document does not exist or failed to retrieve
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // Needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();  // Needed for compass, my location overlays, v6.0.0 and up
    }
}