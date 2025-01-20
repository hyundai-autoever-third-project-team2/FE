package com.twomuchcar.usedcar.service

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import retrofit2.converter.scalars.ScalarsConverterFactory


object ApiClient {
//        private const val BASE_URL = "http://10.0.2.2:8080"
    private const val BASE_URL = "https://twomuchcar.shop"
//    private const val BASE_URL = "https://autoever.site"
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // 응답 내용 로깅
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())  // Gson 대신 Scalars 사용
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}