package com.example.qrcheckin;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QRCodeDataManager {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface QRCodeFetchListener {
        void onQRCodeFetched(List<HashMap<String, String>> qrCodes);
        void onError(Exception e);
    }

    public void fetchQRCodes(QRCodeFetchListener listener) {
        db.collection("event").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HashMap<String, String>> qrCodes = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    HashMap<String, String> qrCodeData = new HashMap<>();
                    qrCodeData.put("image", document.getString("checkinQRCode"));
                    qrCodeData.put("eventID", document.getId());
                    qrCodes.add(qrCodeData);
                }
                listener.onQRCodeFetched(qrCodes);
            } else {
                listener.onError(task.getException());
            }
        });
    }
}

