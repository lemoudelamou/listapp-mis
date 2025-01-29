package com.example.listapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.listapp.Fragments.ConfirmDeleteDialogFragment;
import com.example.listapp.Model.MediaItem;
import com.example.listapp.R;
import com.example.listapp.Utils.NavigationUtils;
import com.example.listapp.ViewModel.MainViewModel;
import com.example.listapp.databinding.ActivityMediaReviewBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class MediaReviewActivity extends AppCompatActivity implements ConfirmDeleteDialogFragment.OnDeleteConfirmedListener {

    private ActivityMediaReviewBinding binding;
    private DrawerLayout drawerLayout;
    private int mediaId;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawerLayout = binding.drawerLayout;
        setupDrawer();

        Intent intent = getIntent();
        title = intent.getStringExtra("mediaTitle");
        String imageUrl = intent.getStringExtra("mediaImageUrl");
        mediaId = intent.getIntExtra("mediaId", -1);


        binding.titleTextView.setText(title);
        Glide.with(this).load(imageUrl).centerCrop().into(binding.imageView);

        binding.deleteIcon.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });


        binding.backIcon.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        NavigationView navigationView = binding.navigationView;
        NavigationUtils.setupNavigationView(navigationView, drawerLayout, this, mainViewModel);

        binding.menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(binding.navigationView));
    }

    private void showDeleteConfirmationDialog() {
        ConfirmDeleteDialogFragment dialog = ConfirmDeleteDialogFragment.newInstance(title);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "ConfirmDeleteDialog");
    }

    @Override
    public void onDeleteConfirmed() {
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        MediaItem mediaItemToDelete = null;
        for (MediaItem item : Objects.requireNonNull(viewModel.getMediaList().getValue())) {
            if (item.getId() == mediaId) {
                mediaItemToDelete = item;
                break;
            }
        }

        if (mediaItemToDelete != null) {
            viewModel.deleteMediaItem(mediaItemToDelete);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("delete", true);
        resultIntent.putExtra("mediaId", mediaId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }



    @Override
    public void onDeleteCancelled() {
    }


}
