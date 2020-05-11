package com.atuma.appruvechallenge.network

import com.atuma.appruvechallenge.network.model.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ApiService {

    @Multipart
    @POST("file_upload")
    fun uploadImage(@Part image: MultipartBody.Part?, @Part("user_id") requestBody: RequestBody): Call<Response?>?


}