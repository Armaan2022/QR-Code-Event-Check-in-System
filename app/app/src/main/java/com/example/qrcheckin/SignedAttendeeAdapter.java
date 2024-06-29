package com.example.qrcheckin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * An ArrayAdapter designed for displaying  Signed-in attendees in a ListView.
 * This adapter is responsible for creating list item views that contain the signed in attendees profiles
 */
public class SignedAttendeeAdapter extends ArrayAdapter<Profile> {
    private Context context;
    private ArrayList<Profile> attendees;

    /**
     * Constructs a new SignedAttendeeAdapter.
     *
     * @param context The current context. Used to inflate the layout file.
     * @param attendees A list of Profile objects.
     */
    public SignedAttendeeAdapter(@NonNull Context context, ArrayList<Profile> attendees) {
        super(context,0, attendees);
        this.attendees = attendees;
        this.context = context;
    }

    /**
     * Provides a view for an AdapterView
     * This method inflates a layout from XML and populates it with the data for the Profile object
     * @param position The position of the item.
     * @param convertView The old view to reuse.
     * @param parent The parent that this view will eventually be attached to.
     * @return A new View
     */

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.signed_attendee_content,
                    parent, false);
        } else {
            view = convertView;
        }
        Profile signedAttendee = getItem(position);
        TextView signedAttendeeName = view.findViewById(R.id.attendee_text);

        assert signedAttendee != null;
        signedAttendeeName.setText(signedAttendee.getUserName());
        return view;
    }
}
