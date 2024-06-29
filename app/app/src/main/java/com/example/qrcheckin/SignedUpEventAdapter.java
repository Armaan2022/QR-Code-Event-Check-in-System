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
 * An ArrayAdapter custom implementation to display a list of signed-up events.
 * This adapter is responsible for converting an Event object into a View
 * @see ArrayAdapter<Event> Event Adapter
 */

public class SignedUpEventAdapter extends ArrayAdapter<Event> {
    /**
     * Constructor for the SignedUpEventAdapter.
     *
     * @param context The current context used to inflate the layout file.
     * @param events An ArrayList of Event objects to be displayed.
     */
    public SignedUpEventAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
    }
    /**
     * Provides a view for an AdapterView
     *
     * This method gets a View that displays the data in the data set.
     * @param position The position in the data set of the item for which the View is needed.
     * @param convertView The old view to reuse, if possible.
     * @return A View belonging to the event
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.signed_up_event_content,
                    parent, false);
        } else {
            view = convertView;
        }
        Event event = getItem(position);
        //prefill the texts
        TextView eventName = view.findViewById(R.id.event_text);
        TextView checkStatus = view.findViewById(R.id.checkIn_text);

        eventName.setText(event.getName());
        checkStatus.setText(event.isCheckInStatus()?"CheckedIn":"");

        return view;
    }
}
