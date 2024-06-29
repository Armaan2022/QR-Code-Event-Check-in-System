package com.example.qrcheckin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * A DialogFragment subclass used to add an announcement. This class provides a dialog
 * where users can enter text for a new announcement.
 */
public class AddAnnouncementFragment extends DialogFragment {
    interface AddAnnouncementDialogListener {

        /**
         * Called when an announcement is added.
         * @param announcement The new announcement that was added.
         */
        void addAnnouncement(Announcement announcement);

        void addNotification(Announcement announcement);
    }
    private AddAnnouncementDialogListener listener;

    /**
     * Called when a fragment is first attached to its context.
     * @throws RuntimeException If the context has not implemented the interface.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddAnnouncementDialogListener) {
            listener = (AddAnnouncementDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement AddAnnouncementDialogListener");
        }
    }

    /**
     * Called when a fragment is first attached to its context.
     * @throws RuntimeException If the context has not implemented the interface.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_announcement, null);
        EditText announcementText = view.findViewById(R.id.add_announcement_text);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setView(view).setTitle("Add an announcement").setNegativeButton("Send as Notification", (dialog, which) -> {
                    String announcement = announcementText.getText().toString();
                    if (announcement.isEmpty()){
                        announcementText.setError("Enter an announcement");
                    } else {
                        listener.addNotification(new Announcement(announcement));
                    }
                })
                .setPositiveButton("Add", (dialog, which) -> {
                    String announcement = announcementText.getText().toString();
                    if (announcement.isEmpty()){
                        announcementText.setError("Enter an announcement");
                    } else {
                        listener.addAnnouncement(new Announcement(announcement));
                    }
                }).create();
    }
}
