package com.twomuchcar.usedcar.service

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
//    private const val BASE_URL = "https://autoever.site"
    private const val BASE_URL = "https://twomuchcar.shop"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Cookie", "AccessToken=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6Imtha2FvIDM4ODA1NzEzODAiLCJyb2xlIjoiUk9MRV9VU0VSIiwiZW1haWwiOiJraW1ob25ndmluQG5hdmVyLmNvbSIsImlhdCI6MTczNzM2OTM1MCwiZXhwIjoxNzM3NDU1NzUwfQ.bjTDf62wnlyY02aFQjYytNqqHzvx8bZsjp_eXX80ogo")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
