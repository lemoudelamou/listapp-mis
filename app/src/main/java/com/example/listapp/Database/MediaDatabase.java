package com.example.listapp.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.listapp.DAO.MediaItemDao;
import com.example.listapp.Model.MediaItem;

@Database(entities = {MediaItem.class}, version = 4)
public abstract class MediaDatabase extends RoomDatabase {
    public abstract MediaItemDao mediaItemDao();

    private static volatile MediaDatabase INSTANCE;

    public static MediaDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MediaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MediaDatabase.class, "media_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
