package com.example.qrcheckin;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * A class defined to get the initials of a profile user name and generate the bitmap of the initials
 */
public class ProfileImageGenerator {

    /**
     * Helper function to parse the initials from the users entered name.
     *
     * @param name, e.g John Smith
     * @return String, users initials eg JS
     */
    public static String getInitials(String name) {
        StringBuilder initials = new StringBuilder();
        for (String s : name.split("\\s+")) {
            initials.append(s.charAt(0));
        }
        return initials.toString();
    }

    /**
     * Function to deterministically generate profile picture.
     * @return Bitmap, based on users initials
     * @param initials initials of the username
     */
    // OpenAI, 2024, ChatGPT, Convert string to bitmap in Android Studio
    public static Bitmap generateInitialsImage(String initials) {
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
}
