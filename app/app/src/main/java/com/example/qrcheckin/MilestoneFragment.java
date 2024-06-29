package com.example.qrcheckin;

import android.annotation.SuppressLint;
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
 * A {@link DialogFragment} subclass used to add a milestone. This fragment is a dialog to the user
 * to enter a milestone number which gives a notification whenever the number of checked in users are reached.
 */
public class MilestoneFragment extends DialogFragment {

    interface AddMilestoneDialogListener {
        void addMilestone(int numAttendee);
    }

    private AddMilestoneDialogListener listener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MilestoneFragment.AddMilestoneDialogListener) {
            listener = (MilestoneFragment.AddMilestoneDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement AddMilestoneDialogListener");
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment Milestone.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_milestone, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText milestoneText = view.findViewById(R.id.add_milestone_text);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setView(view).setTitle("Add a milestone").setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> {
                    String milestone1 = milestoneText.getText().toString();
                    Integer milestone = Integer.parseInt(milestone1);
                    listener.addMilestone(milestone);
                }).create();
    }
}