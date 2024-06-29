package com.example.qrcheckin.SendNotificationPack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.qrcheckin.MainActivity;
import com.example.qrcheckin.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Medium, 2024, source: https://medium.com/@KaushalVasava/push-notification-in-android-how-its-work-2679d0bc0720
// Firebase.google, 2024, https://firebase.google.com/docs/cloud-messaging/android/receive?_gl=1*1ytbkfj*_up*MQ..*_ga*MTE5MjUwOTY4NS4xNzEyNTU1Njcz*_ga_CW55HF8NVT*MTcxMjU1NTY3My4xLjAuMTcxMjU1NTcwNS4wLjAuMA..
/**
 * MyFirebaseMessagingService extends FirebaseMessagingService to handle Firebase Cloud Messaging events.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String message, title;
    String mainUserID;
    private FirebaseFirestore db;

    /**
     *  Called when a new token for the Firebase Cloud Messaging instance is generated.
     * @param token The token used for sending messages to this application instance. This token is
     *     the same as the one retrieved by {@link FirebaseMessaging#getToken()}.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token);

        db = FirebaseFirestore.getInstance();

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


        if (mainUserID != null) {
            db.collection("user").document(mainUserID).update(userInfo).addOnCompleteListener(result -> {
                if (result.isSuccessful()) {
                    Log.d("TOKEN", "Token updated successfully");

                } else {
                    Log.e("TOKEN", "Token not updated successfully");
                    // Optionally show an error message to the user
                }
            });
            // Perform Firestore operation using the document path
        } else {
            Log.d("DEBUG", "User ID not found");
        }
        // Store the user token in firebase
    }

    /**
     * Called when a new FCM message is received.
     * @param remoteMessage Remote message that has been received.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        String message = remoteMessage.getData().get("Message");
        sendNotification(message);
    }

    /**
     * Sends a notification with the given message body.
     * @param messageBody the message data to be sent
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_notif)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
