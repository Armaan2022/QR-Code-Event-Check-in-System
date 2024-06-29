package com.example.qrcheckin.SendNotificationPack;

import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * An interface representing the API service for sending notifications.
 */
public interface APIService {
    @Headers(
            {
                "Content-Type:application/json",
                "Authorization:key=AAAA5OnA8I0:APA91bFw1iUoNZgCggcXf4C2q2D-ik5mUtoZvf-hpl-eZTG6qHgZowwm4NvKNPbRTr-ss9dy4jB1-xJQBOVjmvg4R27wpysMFV3hA0M9OxLBQwT96AYk7lRT-O322BVpmav60eUJRUa4"
            }
    )
    @POST("fcm/send")

    /**
     * Sends a notification using the FCM (Firebase Cloud Messaging) service.
     * @param body The notification sender object containing the notification data and recipient.
     * @return A Call object representing the response from the FCM service.
     */
    Call<MyResponse> sendNotification(@Body NotificationSender body);
}
