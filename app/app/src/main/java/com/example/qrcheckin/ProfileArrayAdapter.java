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
 * An ArrayAdapter customized to display Event objects within a ListView.
 * This adapter takes an ArrayList of Event objects and creates a new view
 * of each event objects.
 */
public class ProfileArrayAdapter extends ArrayAdapter<Profile> {
    public ProfileArrayAdapter(Context context, ArrayList<Profile> profiles) {
        super(context, 0, profiles);
    }
    /**
     * Provides a view for the listview
     * @param position The position of the data item we want to view.
     * @param convertView The old view to reuse.
     * @param parent The parent that this view will eventually be attached to.
     * @return View
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.profile_content,
                    parent, false);
        } else {
            view = convertView;
        }
        Profile profile = getItem(position);
        //prefill the texts
        TextView profileName = view.findViewById(R.id.profile_text);
        //TextView organizerName = view.findViewById(R.id.organizer_text);

        profileName.setText(profile.getUserName());
        //organizerName.setText(event.getOrganizerID());

        return view;
    }
}
