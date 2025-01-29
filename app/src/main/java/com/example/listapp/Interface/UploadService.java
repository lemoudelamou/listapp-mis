package com.example.listapp.Interface;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadService {
    @Multipart
    @POST("/api/upload")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part image);
}
