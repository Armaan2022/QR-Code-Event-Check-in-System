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
 * An ArrayAdapter designed for displaying  Announcement objects in a ListView.
 * This adapter is responsible for creating list item views that contain the announcements
 */
public class AnnouncementsAdapter extends ArrayAdapter<Announcement> {
    /**
     * Constructs a new {@link AnnouncementsAdapter}.
     *
     * @param context The current context.
     * @param announcements An ArrayList of {@link Announcement}
     */
    public AnnouncementsAdapter(Context context, ArrayList<Announcement> announcements) {
        super(context, 0, announcements);
    }

    /**
     * This method inflates a layout from XML and populates it with data for the
     * {@link Announcement} object.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.announcement_content, parent, false);
        } else {
            view = convertView;
        }
        Announcement announcement = getItem(position);
        TextView announcementMsg = view.findViewById(R.id.announcement_text);

        //assert announcement != null;
        announcementMsg.setText(announcement.getAnnouncement());
        return view;
    }
}
