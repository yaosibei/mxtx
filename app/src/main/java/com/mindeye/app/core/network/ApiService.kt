package com.mindeye.app.core.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = "http://10.0.2.2:8000/"

class ApiService(
    private val retrofit: Retrofit
) {
    private val _multimodalService: MultimodalApiService by lazy {
        retrofit.create(MultimodalApiService::class.java)
    }

    private val _communityService: CommunityApiService by lazy {
        retrofit.create(CommunityApiService::class.java)
    }

    fun getMultimodalService() = _multimodalService
    fun getCommunityService() = _communityService

    companion object {
        fun create(baseUrl: String = BASE_URL): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return ApiService(retrofit)
        }
    }
}
