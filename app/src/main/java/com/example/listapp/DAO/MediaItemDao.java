package com.example.listapp.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.listapp.Model.MediaItem;
import java.util.List;

@Dao
public interface MediaItemDao {
    @Insert
    void insert(MediaItem mediaItem);

    @Update
    void update(MediaItem mediaItem);

    @Delete
    void delete(MediaItem mediaItem);

    @Query("SELECT * FROM media_items")
    LiveData<List<MediaItem>> getAllMediaItems();

    @Query("DELETE FROM media_items WHERE id = :id")
    void deleteById(int id);
}
