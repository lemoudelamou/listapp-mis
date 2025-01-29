package com.example.listapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import com.example.listapp.Model.MediaItem;
import com.example.listapp.R;
import com.example.listapp.Utils.NavigationUtils;
import com.example.listapp.ViewModel.MainViewModel;
import com.example.listapp.databinding.ActivityMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private GoogleMap googleMap;
    private ActivityMapBinding binding;
    private DrawerLayout drawerLayout;
    private ExecutorService executorService;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navigationView;
        NavigationUtils.setupNavigationView(navigationView, drawerLayout, this, mainViewModel);
        setupDrawer();
        executorService = Executors.newSingleThreadExecutor();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setOnMarkerClickListener(marker -> {
            zoomToMarker(marker.getPosition());
            return false;
        });
        googleMap.setOnInfoWindowClickListener(this);

        mainViewModel.getMediaList().observe(this, this::updateMarkers);
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        MediaItem mediaItem = (MediaItem) marker.getTag();
        if (mediaItem != null) {
            navigateToMediaReview(mediaItem);
        }
    }

    public void updateMarkers(List<MediaItem> newMediaItems) {
        if (googleMap != null) {
            runOnUiThread(() -> googleMap.clear());
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (MediaItem item : newMediaItems) {
                LatLng location = new LatLng(item.getLatitude(), item.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(item.getTitle())
                        .icon(BitmapDescriptorFactory.defaultMarker(new Random().nextFloat() * 360.0f));
                runOnUiThread(() -> {
                    Marker marker = googleMap.addMarker(markerOptions);
                    if (marker != null) marker.setTag(item);
                });
                builder.include(location);
            }
            if (!newMediaItems.isEmpty()) {
                LatLngBounds bounds = builder.build();
                runOnUiThread(() -> googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)));
            }
        }
    }

    private void navigateToMediaReview(MediaItem mediaItem) {
        Intent intent = new Intent(this, MediaReviewActivity.class);
        intent.putExtra("mediaId", mediaItem.getId());
        intent.putExtra("mediaTitle", mediaItem.getTitle());
        intent.putExtra("mediaImageUrl", mediaItem.getImageSource());
        startActivity(intent);
    }

    public void zoomToMarker(LatLng location) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
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
        binding.menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(binding.navigationView));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
