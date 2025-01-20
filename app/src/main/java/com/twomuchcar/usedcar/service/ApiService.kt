package com.twomuchcar.usedcar.service

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @PUT("/user/fcmToken")
    suspend fun updateFcmToken(@Body request: TokenRequest): Response<Unit>

    @Multipart
    @POST("/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): String
}

