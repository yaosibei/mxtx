package com.mindeye.app.di

import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.mindeye.app.core.sensor.AudioLevelListener
import com.mindeye.app.core.sensor.AppSensorManager
import com.mindeye.app.core.feedback.VibrationManager
import com.mindeye.app.core.location.LocationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationRequest(): com.google.android.gms.location.LocationRequest {
        return com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).build()
    }

    @Provides
    @Singleton
    fun provideLocationService(@ApplicationContext context: Context): LocationService {
        return LocationService(context)
    }

    @Provides
    @Singleton
    fun provideVibrationManager(@ApplicationContext context: Context): VibrationManager {
        return VibrationManager(context)
    }

    @Provides
    @Singleton
    fun provideAudioLevelListener(@ApplicationContext context: Context): AudioLevelListener {
        return AudioLevelListener(context)
    }

    @Provides
    @Singleton
    fun provideAppSensorManager(@ApplicationContext context: Context): AppSensorManager {
        return AppSensorManager(context)
    }

    @Provides
    @Singleton
    fun provideCameraExecutor(): Executor {
        return Executors.newSingleThreadExecutor()
    }

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Provides
    @Singleton
    fun provideMainThreadExecutor(): Executor {
        return Executor { command ->
            android.os.Handler(Looper.getMainLooper()).post(command)
        }
    }
}
