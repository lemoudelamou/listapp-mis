package com.example.listapp.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.example.listapp.Api.ApiClient;
import com.example.listapp.Interface.UploadService;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class ImageHandler {

    @SuppressLint("NewApi")
    public static Uri saveImageLocally(Context context, Uri imageUri) {
        if (imageUri == null) {
            Log.e("ImageHandler", "Image URI is null");
            return null;
        }

        File storageDir = context.getFilesDir();
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(storageDir, fileName);

        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
             OutputStream outputStream = Files.newOutputStream(imageFile.toPath())) {

            if (inputStream == null) {
                Log.e("ImageHandler", "Failed to open InputStream for URI: " + imageUri);
                return null;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            Uri savedImageUri = Uri.fromFile(imageFile);
            Log.d("ImageHandler", "Saved image URI: " + savedImageUri.toString());
            return savedImageUri;

        } catch (IOException e) {
            Log.e("ImageHandler", "Error saving image", e);
            Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static Uri uploadImageToServer(Context context, Uri imageUri) {
        Uri imageUrl = null;

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

            if (inputStream == null) {
                Log.e("ImageHandler", "InputStream is null for URI: " + imageUri);
                return null;
            }

            byte[] imageData;
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] temp = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(temp)) != -1) {
                    buffer.write(temp, 0, bytesRead);
                }
                imageData = buffer.toByteArray();
            }

            MediaType mediaType = MediaType.get("image/*");
            RequestBody requestFile = RequestBody.create(imageData, mediaType);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

            UploadService uploadService = ApiClient.getInstance().create(UploadService.class);
            Response<ResponseBody> response = uploadService.uploadImage(body).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                Log.d("ImageHandler", "Upload successful. Server response: " + responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject data = jsonResponse.optJSONObject("data");

                if (data != null) {
                    String filename = data.optString("file", "");
                    imageUrl = Uri.parse("http://10.0.2.2:7077/" + filename);
                    Log.d("ImageHandler", "Image URL: " + imageUrl);
                }
            } else {
                Log.e("ImageHandler", "Upload failed: " + (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
            }
        } catch (Exception e) {
            Log.e("ImageHandler", "Error during upload: " + e.getMessage(), e);
        }

        return imageUrl;
    }

    public static double[] extractMetadataFromImage(Context context, Uri imageUri) {
        double[] coordinates = new double[2];

        //BHT
        double defaultLatitude = 52.5446621;
        double defaultLongitude = 13.3552574;

        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            if (inputStream == null) {
                Log.e("ImageHandler", "InputStream is null for URI: " + imageUri);
                coordinates[0] = defaultLatitude;
                coordinates[1] = defaultLongitude;
                return coordinates;
            }

            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            boolean gpsDataFound = false;

            for (Directory directory : metadata.getDirectories()) {
                if (directory instanceof GpsDirectory) {
                    GpsDirectory gpsDirectory = (GpsDirectory) directory;
                    if (gpsDirectory.containsTag(GpsDirectory.TAG_LATITUDE) &&
                            gpsDirectory.containsTag(GpsDirectory.TAG_LONGITUDE)) {
                        coordinates[0] = gpsDirectory.getGeoLocation().getLatitude();
                        coordinates[1] = gpsDirectory.getGeoLocation().getLongitude();
                        Log.d("ImageHandler", "GPS Coordinates - Latitude: " + coordinates[0] + ", Longitude: " + coordinates[1]);
                        gpsDataFound = true;
                        break;
                    }
                }
            }

            if (!gpsDataFound) {
                Log.d("ImageHandler", "No GPS data found, using default location.");
                coordinates[0] = defaultLatitude;
                coordinates[1] = defaultLongitude;
            }

        } catch (Exception e) {
            Log.e("ImageHandler", "Error reading metadata: " + e.getMessage(), e);
            coordinates[0] = defaultLatitude;
            coordinates[1] = defaultLongitude;
        }

        return coordinates;
    }


    public static String getImageFileName(Context context, Uri uri) {
        String[] projection = {android.provider.MediaStore.Images.Media.DISPLAY_NAME};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DISPLAY_NAME));
                return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            }
        }
        return "unknown";
    }


}
