package com.example.listapp.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.listapp.DAO.MediaItemDao;
import com.example.listapp.Database.MediaDatabase;
import com.example.listapp.Model.MediaItem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private final MediaItemDao mediaItemDao;
    private final LiveData<List<MediaItem>> mediaList;
    private final MutableLiveData<List<MediaItem>> filteredMediaList = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int currentFilter = 2;

    public MainViewModel(Application application) {
        super(application);
        MediaDatabase database = MediaDatabase.getDatabase(application);
        mediaItemDao = database.mediaItemDao();
        mediaList = mediaItemDao.getAllMediaItems();

        mediaList.observeForever(this::filterMediaItems);
    }

    public LiveData<List<MediaItem>> getMediaList() {
        return mediaList;
    }

    public LiveData<List<MediaItem>> getFilteredMediaList() {
        return filteredMediaList;
    }

    public void setFilter(int filter) {
        currentFilter = filter;
        List<MediaItem> currentItems = mediaList.getValue();
        if (currentItems != null) {
            filterMediaItems(currentItems);
        }
    }

    private void filterMediaItems(List<MediaItem> allItems) {
        if (allItems != null) {
            List<MediaItem> filteredItems = new ArrayList<>();
            for (MediaItem item : allItems) {
                if (currentFilter == 2 ||
                        (currentFilter == 0 && item.isLocalStorage()) ||
                        (currentFilter == 1 && !item.isLocalStorage())) {
                    filteredItems.add(item);
                }
            }
            filteredMediaList.setValue(filteredItems);
        }
    }

    public void addMediaItem(MediaItem mediaItem) {
        executorService.execute(() -> mediaItemDao.insert(mediaItem));
    }

    public void updateMediaItem(MediaItem mediaItem) {
        executorService.execute(() -> mediaItemDao.update(mediaItem));
    }

    public void deleteMediaItem(MediaItem mediaItem) {
        executorService.execute(() -> mediaItemDao.delete(mediaItem));
    }

    public void deleteMediaItemById(int id) {
        executorService.execute(() -> mediaItemDao.deleteById(id));
    }
}
