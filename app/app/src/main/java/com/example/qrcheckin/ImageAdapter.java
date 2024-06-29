package com.example.qrcheckin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Custom ArrayAdapter for displaying images within a GridView.
 * This adapter handles the conversion of strings to Bitmap images and resizes them before
 * displaying in an ImageView.
 */
public class ImageAdapter extends ArrayAdapter<HashMap> {

    /**
     * Constructs a new {@code ImageAdapter} with the list of images
     *
     * @param context The current context.
     * @param profiles An ArrayList of HashMap objects, each containing Base64 image string
     */
    public ImageAdapter(Context context, ArrayList<HashMap> profiles) {
        super(context, 0, profiles);
    }

    /**
     * Provides a view for an AdapterView
     * This method gets a View that displays the data at the specified position.
     * The image from each profile's HashMap is converted to a Bitmap,
     * and then set into an ImageView.
     *
     * @param position The position of the item in the dataset.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.image_item, parent, false);
        }

        HashMap profile = getItem(position);

        // Convert base64 string to Bitmap
        Bitmap bitmap = Helpers.base64ToBitmap((String) profile.get("image"));

        // Resize the bitmap
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 200, 200); // Specify your desired width and height

        // Set the resized bitmap to the ImageView
        ImageView profileImageView = view.findViewById(R.id.image_view);
        profileImageView.setImageBitmap(resizedBitmap);

        return view;
    }

    // OpenAI, 2024, ChatGPT, How to resize a bitmap
    /**
     * Resizes a bitmap image to the specific width and height.
     * @param bitmap The original bitmap image.
     * @param newWidth The desired new width of the bitmap.
     * @param newHeight The desired new height of the bitmap.
     * @return A resized Bitmap object.
     */
    private Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width * 2;
        float scaleHeight = ((float) newHeight) / height * 2;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }
}