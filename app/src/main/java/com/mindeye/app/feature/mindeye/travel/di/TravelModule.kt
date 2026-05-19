package com.mindeye.app.feature.mindeye.travel.di

import com.google.gson.GsonBuilder
import com.mindeye.app.core.network.BASE_URL
import com.mindeye.app.feature.mindeye.travel.data.XunfeiTtsManager
import com.mindeye.app.feature.mindeye.travel.network.TravelApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TravelModule {

    @Provides
    @Singleton
    fun provideXunfeiTtsManager(client: OkHttpClient): XunfeiTtsManager {
        return XunfeiTtsManager(client)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideTravelApiService(client: OkHttpClient): TravelApiService {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TravelApiService::class.java)
    }
}
