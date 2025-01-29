package com.example.listapp.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.listapp.R;
import com.example.listapp.databinding.DeleteConfirmationBinding;


public class ConfirmDeleteDialogFragment extends DialogFragment {

    private String title;
    private OnDeleteConfirmedListener listener;

    public interface OnDeleteConfirmedListener {
        void onDeleteConfirmed();
        void onDeleteCancelled();
    }

    public static ConfirmDeleteDialogFragment newInstance(String title) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString("title");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DeleteConfirmationBinding binding = DeleteConfirmationBinding.inflate(getLayoutInflater());

        binding.dialogTitle.setText(R.string.medium_loeschen);
        binding.confirmationMessage.setText(String.format("Möchten Sie das Medium %s löschen ?", title));

        binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {

                    listener.onDeleteConfirmed();

            }
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteCancelled();
            }
            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());
        return builder.create();
    }

    public void setListener(OnDeleteConfirmedListener listener) {
        this.listener = listener;
    }
}
