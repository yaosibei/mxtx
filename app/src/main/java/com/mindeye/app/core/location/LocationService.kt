package com.mindeye.app.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 位置服务 — 持有 callback 引用以正确停止定位。
 */
class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    suspend fun getCurrentLocation(): Location? {
        return suspendCoroutine { continuation ->
            if (!hasLocationPermission()) {
                Log.w(TAG, "缺少位置权限")
                continuation.resume(null)
                return@suspendCoroutine
            }
            
            // 优先尝试获取最近一次已知位置，速度最快
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                        continuation.resume(location)
                    } else {
                        // 如果 lastLocation 为空，则请求当前最新位置
                        requestFreshLocation(continuation)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "获取最近位置失败，尝试请求新位置", e)
                    requestFreshLocation(continuation)
                }
        }
    }

    private fun requestFreshLocation(continuation: kotlin.coroutines.Continuation<Location?>) {
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return
        }
        
        val currentRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(60000) // 接受 1 分钟内的缓存
            .build()
            
        fusedLocationClient.getCurrentLocation(currentRequest, null)
            .addOnSuccessListener { location ->
                currentLocation = location
                continuation.resume(location)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "请求当前位置失败", e)
                continuation.resume(null)
            }
    }

    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            Log.w(TAG, "缺少位置权限")
            close()
            return@callbackFlow
        }
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    currentLocation = location
                    trySend(location)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.w(TAG, "缺少位置权限")
            return
        }
        if (locationCallback != null) return // 已在运行

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    currentLocation = location
                    Log.d(TAG, "位置更新：lat=${location.latitude}, lng=${location.longitude}")
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
    }

    fun getLastLocation(): Location? = currentLocation

    fun isAtIntersection(): Boolean {
        currentLocation?.let { loc ->
            // 简单启发式：如果 5 秒内移动距离 < 3 米，可能在路口等待
            // 实际应接入地图 SDK 的道路数据
            return loc.accuracy < 20 && loc.speed < 0.5f
        }
        return false
    }

    fun isOnSidewalk(): Boolean {
        return currentLocation != null && currentLocation!!.accuracy < 30
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "LocationService"
    }
}
