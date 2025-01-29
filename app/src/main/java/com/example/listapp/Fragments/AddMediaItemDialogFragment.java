package com.example.listapp.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.example.listapp.R;
import com.example.listapp.Utils.ImageHandler;
import com.example.listapp.databinding.DialogAddMediaItemBinding;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AddMediaItemDialogFragment extends DialogFragment {


    public interface AddMediaItemDialogListener {
        void onMediaItemCreated(String title, Uri imageUri, boolean isLocalStorage, double latitude, double longitude);
        void onMediaItemUpdated(String title, Uri image, double latitude, double longitude);
        void onMediaItemDeleted();
    }

    private AddMediaItemDialogListener listener;
    private boolean isEditing;
    private String existingTitle;
    private Uri existingImageUri;
    private Uri imageUri;
    private RadioGroup storageOptionGroup;
    private boolean isLocalStorage;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    private DialogAddMediaItemBinding binding;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @NonNull
    public static AddMediaItemDialogFragment newInstance(boolean isEditing,
                                                         @Nullable String existingTitle,
                                                         @Nullable Uri existingImageUri, boolean isLocalStorage) {
        AddMediaItemDialogFragment fragment = new AddMediaItemDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("isEditing", isEditing);
        args.putString("existingTitle", existingTitle);
        args.putParcelable("existingImageUri", existingImageUri);
        args.putBoolean("isLocalStorage", isLocalStorage);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(AddMediaItemDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isEditing = requireArguments().getBoolean("isEditing");
        existingTitle = requireArguments().getString("existingTitle");
        existingImageUri = requireArguments().getParcelable("existingImageUri");
        isLocalStorage = requireArguments().getBoolean("isLocalStorage");


        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            requireActivity();
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();

                assert imageUri != null;
                Log.d("ImageUri", "Selected image URI: " + imageUri);

                if (binding != null) {
                    binding.displayImage.setImageURI(imageUri);
                }
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddMediaItemBinding.inflate(getLayoutInflater());
        storageOptionGroup = binding.storageOptionGroup;
        binding.storageOptionLocal.setChecked(true);

        if (isEditing) {
            (isLocalStorage ? binding.storageOptionLocal : binding.storageOptionRemote).setChecked(true);

            storageOptionGroup.setEnabled(false);
            for (int i = 0; i < storageOptionGroup.getChildCount(); i++) {
                storageOptionGroup.getChildAt(i).setEnabled(false);
            }
        }

        binding.dialogTitle.setText(isEditing ? getString(R.string.medium_editieren) : getString(R.string.neues_medium_));

        if (isEditing && existingTitle != null) {
            binding.addTitle.requestFocus();

            binding.addTitle.setText(existingTitle);
        }

        Glide.with(requireContext())
                .load(existingImageUri != null ? existingImageUri : android.R.drawable.ic_menu_gallery)
                .into(binding.displayImage);

        binding.addTitle.requestFocus();

        binding.addButton.setText(isEditing ? getString(R.string.modifizieren) : getString(R.string.erstellen));
        binding.deleteButton.setText(getString(R.string.loeschen));
        binding.deleteButton.setEnabled(isEditing);
        binding.deleteButton.setAlpha(isEditing ? 1.0f : 0.5f);

        binding.addButton.setOnClickListener(v -> handleSave());
        binding.deleteButton.setOnClickListener(v -> handleDelete());
        binding.pickImage.setOnClickListener(v -> openImagePicker());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleSave() {
        String title = Objects.requireNonNull(binding.addTitle.getText()).toString().trim();

        if (TextUtils.isEmpty(title)) {
            if (imageUri != null) {
                title = ImageHandler.getImageFileName(requireContext(), imageUri);
                String finalTitle = title;
                requireActivity().runOnUiThread(() -> binding.addTitle.setText(finalTitle));
            } else {
                requireActivity().runOnUiThread(() -> binding.addTitle.setError(getString(R.string.titel_darf_nicht_leer_sein)));
                return;
            }
        }
        if (imageUri == null && existingImageUri == null) {
            requireActivity().runOnUiThread(() -> {
                binding.errorMessage.setVisibility(View.VISIBLE);
                binding.errorMessage.setText(R.string.bitte_ein_bild_auswaehlen);
            });
            return;
        } else {
            requireActivity().runOnUiThread(() -> binding.errorMessage.setVisibility(View.GONE));
        }
        Uri finalImageUri = imageUri != null ? imageUri : existingImageUri;

        if (!isEditing) {
            handleNewItemSave(title, finalImageUri);
        } else {
            handleExistingItemSave(title, finalImageUri);
        }
    }

    private void handleNewItemSave(String title, Uri finalImageUri) {
        int selectedOption = storageOptionGroup.getCheckedRadioButtonId();

        if (selectedOption == R.id.storage_option_local) {
             finalImageUri = ImageHandler.saveImageLocally(requireContext(), imageUri);

            isLocalStorage = true;

            requireActivity().runOnUiThread(() -> binding.storageOptionLocal.setChecked(true));
            saveToListener(title, finalImageUri);
        } else if (selectedOption == R.id.storage_option_remote) {
            uploadImageInBackground(title, finalImageUri, true);
        }
    }

    private void handleExistingItemSave(String title, Uri finalImageUri) {
        if (imageUri != null) {
            if (isLocalStorage) {
                finalImageUri = ImageHandler.saveImageLocally(requireContext(),imageUri);
                isLocalStorage = true;

                requireActivity().runOnUiThread(() -> {
                    binding.storageOptionLocal.setChecked(true);
                    binding.storageOptionRemote.setChecked(false);
                });
                saveToListener(title, finalImageUri);
            } else {
                uploadImageInBackground(title, imageUri, false);
            }
        } else {
            saveToListener(title, finalImageUri);
        }
    }

    private void uploadImageInBackground(String title, Uri imageUri, boolean isNew) {
        executorService.submit(() -> {
            Uri uploadedUri = ImageHandler.uploadImageToServer(requireContext(), imageUri);

            if (uploadedUri != null) {
                isLocalStorage = false;

                requireActivity().runOnUiThread(() -> {
                    if (listener != null) {
                        double[] coords = ImageHandler.extractMetadataFromImage(requireContext(), imageUri);
                        if (isNew) {
                            listener.onMediaItemCreated(title, uploadedUri, isLocalStorage, coords[0], coords[1]);
                        } else {
                            listener.onMediaItemUpdated(title, uploadedUri, coords[0], coords[1]);
                        }
                    }
                    dismiss();
                });
            } else {
                    requireActivity().runOnUiThread(() -> {
                    binding.errorMessage.setVisibility(View.VISIBLE);
                    binding.errorMessage.setText(R.string.bild_upload_fehlgeschlagen);
                });
            }
        });
    }

    private void saveToListener(String title, Uri finalImageUri) {
        if (listener != null) {
            double[] coords = ImageHandler.extractMetadataFromImage(requireContext(),finalImageUri);
            if (isEditing) {
                listener.onMediaItemUpdated(title, finalImageUri, coords[0], coords[1]);
            } else {
                listener.onMediaItemCreated(title, finalImageUri, isLocalStorage, coords[0], coords[1]);
            }
        }
        dismiss();
    }

    private void handleDelete() {
        if (listener != null) {
            listener.onMediaItemDeleted();
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}