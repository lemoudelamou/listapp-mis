package com.example.listapp.Utils;

import static com.example.listapp.R.string.keine_medien_vorhanden;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.listapp.Activities.MainActivity;
import com.example.listapp.Activities.MapActivity;
import com.example.listapp.Model.MediaItem;
import com.example.listapp.R;
import com.example.listapp.ViewModel.MainViewModel;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class NavigationUtils extends AppCompatActivity {


    public static void setupNavigationView(NavigationView navigationView, DrawerLayout drawerLayout, Context context, MainViewModel mainViewModel) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_medien) {
                if (!(context instanceof MainActivity)) {
                    Intent mainActivityIntent = new Intent(context, MainActivity.class);
                    context.startActivity(mainActivityIntent);
                }

            } else if (itemId == R.id.nav_karte) {
                List<MediaItem> mediaItems = mainViewModel.getMediaList().getValue();
                if (mediaItems != null) {
                    Intent mapIntent = new Intent(context, MapActivity.class);
                    mapIntent.putExtra("mediaItems", new ArrayList<>(mediaItems));
                    context.startActivity(mapIntent);
                } else {
                    Toast.makeText(context, keine_medien_vorhanden, Toast.LENGTH_SHORT).show();
                }
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }
}

