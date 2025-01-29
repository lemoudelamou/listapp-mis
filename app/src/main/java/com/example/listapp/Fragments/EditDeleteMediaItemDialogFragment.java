package com.example.listapp.Fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.listapp.databinding.DialogEditDeleteMediaItemBinding;

public class EditDeleteMediaItemDialogFragment extends DialogFragment {

    private static final String ARG_ITEM_TITLE = "item_title";
    private EditMediaItemListener listener;

    public interface EditMediaItemListener {
        void onEditSelected();
        void onDeleteSelected();
    }

    public static EditDeleteMediaItemDialogFragment newInstance(String itemTitle) {
        EditDeleteMediaItemDialogFragment fragment = new EditDeleteMediaItemDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_TITLE, itemTitle);
        fragment.setArguments(args);
        return fragment;
    }

    public void setEditMediaItemListener(EditMediaItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DialogEditDeleteMediaItemBinding binding = DialogEditDeleteMediaItemBinding.inflate(getLayoutInflater());

        String itemTitle = getArguments() != null ? getArguments().getString(ARG_ITEM_TITLE) : "Edit Item";

        binding.dialogTitle.setText(itemTitle);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();

        binding.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditSelected();
            }
            dialog.dismiss();
        });

        binding.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteSelected();
            }
            dialog.dismiss();
        });

        return dialog;
    }
}
