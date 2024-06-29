package com.example.qrcheckin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper class to generate document IDs for Firebase
 * collections.
 */
public class Helpers {

    public static String createDocID(String field1, String field2, String field3) {
        String combinedString = field1 + field2 + field3;

        try {
            // Create a MessageDigest instance for SHA-256 hashing
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Get the byte array of the combined fields
            byte[] bytes = combinedString.getBytes();

            // Update the digest with the byte array
            digest.update(bytes);

            // Get the hashed byte array
            byte[] hashedBytes = digest.digest();

            // Convert the hashed byte array to a hexadecimal string
            StringBuilder stringBuilder = new StringBuilder();
            for (byte hashedByte : hashedBytes) {
                stringBuilder.append(Integer.toString((hashedByte & 0xff) + 0x100, 16).substring(1));
            }

            // Return the hexadecimal string as the document ID
            return stringBuilder.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Converts bitmap to Base64 string for saving as Firebase field
     * @param bitmap Bitmap to convert to Base64
     * @return String
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        // Source: https://www.thepolyglotdeveloper.com/2015/06/from-bitmap-to-base64-and-back-with-native-android/
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Converts Base64 to bitmap string for saving as Firebase field
     * Source:
     * @param b64 String of ASCII Values
     * @return Bitmap
     */
    //https://www.thepolyglotdeveloper.com/2015/06/from-bitmap-to-base64-and-back-with-native-android/
    public static Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    /**
     * Reverse string helper class. Used to invert the EventID (docID) string which will be used
     * for the promo code. The regular EventID is used for the CheckinQR Code.
     * @param str String to reverse
     * @return String
     */

    public static String reverseString(String str){
        // OpenAI, 2024, ChatGPT, "Reverse String function in java"
        StringBuilder builder = new StringBuilder();

        // append a string into StringBuilder input1
        builder.append(str);

        // reverse StringBuilder input1
        builder.reverse();

        return builder.toString();
    }

    /**
     *Validates the given qr content to check for forbidden characters
     * @param id The qr content
     * @return true or false
     */
    public static boolean isValidDocumentId(String id) {
        // Implement validation logic here.
        // A basic example: ensure the ID is not a URL and does not contain forbidden characters.
        return !id.contains("/") && !id.contains(" ") && !id.startsWith("http://") && !id.startsWith("https://");
    }
}

