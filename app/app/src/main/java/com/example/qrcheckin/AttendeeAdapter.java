package com.example.qrcheckin;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * An ArrayAdapter designed for displaying Profile objects in a ListView.
 */
public class AttendeeAdapter extends ArrayAdapter<Profile> {
    private Context context;
    private ArrayList<Profile> attendees;

    /**
     * Constructs a new AttendeeAdapter.
     *
     * @param context The current context. Used to inflate the layout file.
     * @param attendees A list of Profile objects.
     */
    public AttendeeAdapter(@NonNull Context context, ArrayList<Profile> attendees, String userID, String eventID) {
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
            view = LayoutInflater.from(getContext()).inflate(R.layout.attendee_content,
                    parent, false);
        } else {
            view = convertView;
        }
        Profile attendee = getItem(position);
        TextView attendeeName = view.findViewById(R.id.attendee_text);
        TextView attendeeCount = view.findViewById(R.id.check_in_times_text);
        assert attendee != null;
        attendeeName.setText(attendee.getUserName());
        int stringCount = Math.toIntExact(attendee.getCheckInCount());
        attendeeCount.setText(String.valueOf(stringCount));
        return view;
    }
}
