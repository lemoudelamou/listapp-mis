package com.example.listapp.Model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "media_items")
public class MediaItem implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String imageSource;
    private String date;
    private boolean isLocalStorage;
    private double latitude;
    private double longitude;

    public MediaItem(String title, String imageSource, String date, boolean isLocalStorage, double latitude, double longitude) {
        this.title = title;
        this.imageSource = imageSource;
        this.date = date;
        this.isLocalStorage = isLocalStorage;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected MediaItem(Parcel in) {
        id = in.readInt();
        title = in.readString();
        imageSource = in.readString();
        date = in.readString();
        isLocalStorage = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(imageSource);
        dest.writeString(date);
        dest.writeByte((byte) (isLocalStorage ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isLocalStorage() {
        return isLocalStorage;
    }

    public void setLocalStorage(boolean localStorage) {
        isLocalStorage = localStorage;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
