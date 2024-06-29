package com.example.qrcheckin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * A mock implementation of QRCodeDataManager for testing purposes.
 */
public class FakeQRCodeDataManager extends QRCodeDataManager {
    @Override
    public void fetchQRCodes(QRCodeFetchListener listener) {
        // Simulating successful fetch with predefined data
        List<HashMap<String, String>> fakeQRCodes = new ArrayList<>();
        HashMap<String, String> qrCode1 = new HashMap<>();
        qrCode1.put("image", "fakeQRCodeImageUrl1");
        qrCode1.put("eventID", "fakeEventID1");

        HashMap<String, String> qrCode2 = new HashMap<>();
        qrCode2.put("image", "fakeQRCodeImageUrl2");
        qrCode2.put("eventID", "fakeEventID2");

        fakeQRCodes.add(qrCode1);
        fakeQRCodes.add(qrCode2);

        listener.onQRCodeFetched(fakeQRCodes);
    }
}

