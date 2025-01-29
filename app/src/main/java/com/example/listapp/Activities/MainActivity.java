package com.example.listapp.Activities;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.listapp.Fragments.AddMediaItemDialogFragment;
import com.example.listapp.Fragments.ConfirmDeleteDialogFragment;
import com.example.listapp.Fragments.EditDeleteMediaItemDialogFragment;
import com.example.listapp.Adapter.MediaAdapter;
import com.example.listapp.Model.MediaItem;
import com.example.listapp.Utils.NavigationUtils;
import com.example.listapp.R;
import com.example.listapp.ViewModel.MainViewModel;
import com.example.listapp.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 23;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String FILTER_KEY = "selected_filter";



    private MediaAdapter mediaAdapter;
    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private ActivityResultLauncher<Intent> mediaReviewLauncher;
    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Log.d(TAG, "Manage External Storage Permissions Granted");
                    } else {
                        Toast.makeText(this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        drawerLayout = binding.drawerLayout;

        NavigationUtils.setupNavigationView(binding.navigationView, drawerLayout, this, mainViewModel);
        setupDrawer();
        setupRecyclerView();
        setupListeners();
        observeData();

        restoreFilter();

        mediaReviewLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        boolean isDeleted = data.getBooleanExtra("delete", false);
                        int mediaId = data.getIntExtra("mediaId", -1);

                        if (isDeleted && mediaId != -1) {
                            mainViewModel.deleteMediaItemById(mediaId);
                        }
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleStoragePermissions();
    }

    private void handleStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                storageActivityResultLauncher.launch(getManageStoragePermissionIntent());
            }
        } else {
            requestStoragePermissions();
        }
    }

    private void requestStoragePermissions() {
        if (!hasStoragePermissions()) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, STORAGE_PERMISSION_CODE);
        }
    }

    private boolean hasStoragePermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private Intent getManageStoragePermissionIntent() {
        return new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                .setData(Uri.fromParts("package", getPackageName(), null));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(this, (requestCode == STORAGE_PERMISSION_CODE && hasStoragePermissions())
                ? "Storage Permissions Granted" : "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("NonConstantResourceId")
    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(binding.navigationView));
    }

    private void setupRecyclerView() {
        mediaAdapter = new MediaAdapter(new ArrayList<>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(mediaAdapter);

        mediaAdapter.setOnItemClickListener((mediaItem, position) -> openMediaReviewActivity(mediaItem));
        mediaAdapter.setOnEditClickListener(this::openEditDialog);
    }

    private void setupListeners() {
        binding.addIcon.setOnClickListener(v -> openAddDialog());

        binding.local.setOnClickListener(v -> {
            setAlphaState(0);
            mainViewModel.setFilter(0);
            saveFilter(0);
        });

        binding.remote.setOnClickListener(v -> {
            setAlphaState(1);
            mainViewModel.setFilter(1);
            saveFilter(1);
        });

        binding.both.setOnClickListener(v -> {
            setAlphaState(2);

            mainViewModel.setFilter(2);
            saveFilter(2);
        });
    }


    private void observeData() {
        mainViewModel.getFilteredMediaList().observe(this, mediaAdapter::updateData);
    }

    private void openAddDialog() {
        AddMediaItemDialogFragment addDialog = AddMediaItemDialogFragment.newInstance(
                false,
                null,
                null,
                true
        );

        addDialog.setListener(new AddMediaItemDialogFragment.AddMediaItemDialogListener() {
            @Override
            public void onMediaItemCreated(String title, Uri imageUri, boolean isLocalStorage, double latitude, double longitude) {
                String sub = getFormattedDate();
                MediaItem newItem = new MediaItem(title, imageUri.toString(), sub, isLocalStorage, latitude, longitude);
                mainViewModel.addMediaItem(newItem);
            }

            @Override
            public void onMediaItemUpdated(String title, Uri image, double latitude, double longitude) {
            }

            @Override
            public void onMediaItemDeleted() {
            }


        });

        addDialog.show(getSupportFragmentManager(), "AddMediaItemDialog");
    }


    private void openEditDialog(MediaItem mediaItem, int position) {
        EditDeleteMediaItemDialogFragment chooseDialog = EditDeleteMediaItemDialogFragment.newInstance(mediaItem.getTitle());
        chooseDialog.setEditMediaItemListener(new EditDeleteMediaItemDialogFragment.EditMediaItemListener() {
            @Override
            public void onEditSelected() {
                String imageSource = mediaItem.getImageSource();
                Uri existingUri = imageSource != null ? Uri.parse(imageSource) : null;

                boolean isLocalStorage = mediaItem.isLocalStorage();
                AddMediaItemDialogFragment editDialog = AddMediaItemDialogFragment.newInstance(
                        true,
                        mediaItem.getTitle(),
                        existingUri,
                        isLocalStorage
                );
                Log.d("Image", "image Uri:" + existingUri);

                editDialog.setListener(new AddMediaItemDialogFragment.AddMediaItemDialogListener() {
                    @Override
                    public void onMediaItemCreated(String title, Uri image, boolean isLocalStorage, double lat, double lon) {
                    }

                    @Override
                    public void onMediaItemUpdated(String title, Uri image, double latitude, double longitude) {
                        if (existingUri == null || !existingUri.equals(image)) {
                            mediaItem.setTitle(title);
                            mediaItem.setImageSource(image.toString());
                            mediaItem.setLatitude(latitude);
                            mediaItem.setLongitude(longitude);
                            mainViewModel.updateMediaItem(mediaItem);
                        } else {
                            mediaItem.setTitle(title);
                            mainViewModel.updateMediaItem(mediaItem);
                        }
                    }

                    @Override
                    public void onMediaItemDeleted() {
                        ConfirmDeleteDialogFragment deleteDialog = ConfirmDeleteDialogFragment.newInstance(mediaItem.getTitle());
                        deleteDialog.setListener(new ConfirmDeleteDialogFragment.OnDeleteConfirmedListener() {
                            @Override
                            public void onDeleteConfirmed() {
                                mainViewModel.deleteMediaItem(mediaItem);
                                mediaAdapter.notifyItemRemoved(position);
                                Toast.makeText(MainActivity.this, "Item deleted: " + mediaItem.getTitle(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onDeleteCancelled() {
                                Toast.makeText(MainActivity.this, "Deletion cancelled", Toast.LENGTH_SHORT).show();
                            }
                        });
                        deleteDialog.show(getSupportFragmentManager(), "DeleteConfirmationDialog");
                    }

                });

                editDialog.show(getSupportFragmentManager(), "EditMediaItemDialog");
            }

            @Override
            public void onDeleteSelected() {
                ConfirmDeleteDialogFragment deleteDialog = ConfirmDeleteDialogFragment.newInstance(mediaItem.getTitle());
                deleteDialog.setListener(new ConfirmDeleteDialogFragment.OnDeleteConfirmedListener() {
                    @Override
                    public void onDeleteConfirmed() {
                        mainViewModel.deleteMediaItem(mediaItem);
                        mediaAdapter.notifyItemRemoved(position);
                        Toast.makeText(MainActivity.this, "Item deleted: " + mediaItem.getTitle(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDeleteCancelled() {
                        Toast.makeText(MainActivity.this, "Deletion cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                deleteDialog.show(getSupportFragmentManager(), "DeleteConfirmationDialog");
            }
        });

        chooseDialog.show(getSupportFragmentManager(), "ChooseActionDialog");
    }


    private String getFormattedDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(new Date());
    }


    private void saveFilter(int filterValue) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(FILTER_KEY, filterValue);
        editor.apply();
    }

    private void setAlphaState(int filterValue) {
        binding.local.setAlpha(filterValue == 0 ? 0.5f : 1.0f);
        binding.remote.setAlpha(filterValue == 1 ? 0.5f : 1.0f);
        binding.both.setAlpha(filterValue == 2 ? 0.5f : 1.0f);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(FILTER_KEY, filterValue);
        editor.apply();
    }

    private void restoreFilter() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedFilter = preferences.getInt(FILTER_KEY, 2);

        mainViewModel.setFilter(savedFilter);
        setAlphaState(savedFilter);
    }
    private void openMediaReviewActivity(MediaItem mediaItem) {
        Intent intent = new Intent(this, MediaReviewActivity.class);
        intent.putExtra("mediaTitle", mediaItem.getTitle());
        intent.putExtra("mediaImageUrl", mediaItem.getImageSource());
        intent.putExtra("mediaId", mediaItem.getId());
        mediaReviewLauncher.launch(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
