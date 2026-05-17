# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Retrofit / OkHttp / Gson
-keepattributes Signature, Exceptions
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http* <methods>;
}
-keep class com.mindeye.app.core.network.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }

# TensorFlow Lite
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**

# CameraX
-keep class androidx.camera.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }

# Compose
-keep class androidx.compose.** { *; }
