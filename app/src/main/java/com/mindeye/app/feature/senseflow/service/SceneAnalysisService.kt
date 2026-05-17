package com.mindeye.app.feature.senseflow.service

import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.mindeye.app.core.model.SceneAnalysisResponse
import com.mindeye.app.core.database.AppDatabase
import com.mindeye.app.core.database.entity.SceneRecordEntity
import com.mindeye.app.feature.senseflow.data.SceneRepository
import com.mindeye.app.core.sensor.AudioLevelListener
import com.mindeye.app.core.sensor.AppSensorManager
import com.mindeye.app.core.feedback.VibrationManager
import com.mindeye.app.core.location.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 随境 SenseFlow 场景分析服务。
 * 修复了 LifecycleOwner 崩溃、空白 bitmap、缺失定位等问题。
 */
class SceneAnalysisService(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val locationService: LocationService? = null
) {

    private val TAG = "SceneAnalysisService"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private val sensorManager = AppSensorManager(context)
    private val audioLevelListener = AudioLevelListener(context)
    private val vibrationManager = VibrationManager(context)
    private val sceneRepository = SceneRepository(AppDatabase.getDatabase(context).sceneRecordDao())

    private var isRunning = false
    private var currentSceneType = "indoor_quiet"
    private val handler = Handler(Looper.getMainLooper())
    private val analysisInterval = 3000L

    private var capturedBitmap: Bitmap? = null

    fun start() {
        if (isRunning) return
        isRunning = true
        sensorManager.startListening()
        audioLevelListener.startListening()
        initializeCamera()
        startAnalysisLoop()
        Log.d(TAG, "Scene analysis service started")
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
        sensorManager.stopListening()
        audioLevelListener.stopListening()
        capturedBitmap?.recycle()
        capturedBitmap = null
        Log.d(TAG, "Scene analysis service stopped")
    }

    fun release() {
        stop()
        audioLevelListener.release()
        sensorManager.release()
    }

    private fun initializeCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                preview = Preview.Builder().build()
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
                    capturedBitmap = imageProxy.toBitmap()
                    imageProxy.close()
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )
                Log.d(TAG, "Camera initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing camera: ${e.message}")
                // Fallback: use ImageCapture only
                try {
                    cameraProvider = cameraProviderFuture.get()
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    imageCapture = ImageCapture.Builder().build()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
                } catch (e2: Exception) {
                    Log.e(TAG, "Camera fallback also failed: ${e2.message}")
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setPreviewView(previewView: PreviewView) {
        preview?.setSurfaceProvider(previewView.surfaceProvider)
    }

    private fun startAnalysisLoop() {
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return
                analyzeScene()
                handler.postDelayed(this, analysisInterval)
            }
        })
    }

    private fun analyzeScene() {
        scope.launch {
            try {
                val imageData = captureImage()
                val audioLevel = audioLevelListener.getCurrentAudioLevel().toFloat()
                val isMoving = sensorManager.isMoving()
                val motionState = sensorManager.detectMotionState()
                val location = locationService?.getLastLocation()

                val response = analyzeSceneLocally(audioLevel, isMoving, motionState)
                currentSceneType = response.sceneType
                handleSceneAnalysisResponse(response)

                saveSceneRecord(response, audioLevel.toDouble(), location)
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing scene: ${e.message}")
            }
        }
    }

    private fun captureImage(): String {
        val bitmap = capturedBitmap
        if (bitmap != null && !bitmap.isRecycled && bitmap.width > 10) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        }
        // Fallback: if camera not ready yet, return minimal data
        return ""
    }

    private fun analyzeSceneLocally(
        audioLevel: Float,
        isMoving: Boolean,
        motionState: String
    ): SceneAnalysisResponse {
        val sceneType = when {
            audioLevel < 40f && !isMoving -> "indoor_quiet"
            audioLevel > 65f && !isMoving -> "indoor_noisy"
            audioLevel < 50f && isMoving -> "outdoor_quiet"
            audioLevel > 70f && isMoving -> "outdoor_noisy"
            audioLevel in 50f..70f && motionState == "walking" -> "social"
            audioLevel > 60f && motionState == "vehicle" -> "traffic"
            else -> "indoor_quiet"
        }

        val safetyLevel = when {
            sceneType == "traffic" -> "caution"
            audioLevel > 80f -> "danger"
            sceneType == "outdoor_noisy" -> "caution"
            else -> "safe"
        }

        return SceneAnalysisResponse(
            sceneType = sceneType,
            confidence = 0.8f,
            recommendations = getRecommendationsForScene(sceneType),
            safetyLevel = safetyLevel,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun getRecommendationsForScene(sceneType: String): List<String> = when (sceneType) {
        "indoor_quiet" -> listOf("环境安静，适合语音交互")
        "indoor_noisy" -> listOf("环境嘈杂，建议使用震动反馈")
        "outdoor_quiet" -> listOf("户外环境安静，注意周围安全")
        "outdoor_noisy" -> listOf("户外环境嘈杂，建议使用震动导航")
        "social" -> listOf("检测到社交场景，注意周围人流")
        "traffic" -> listOf("检测到交通场景，注意交通安全")
        else -> listOf("请小心行走")
    }

    private fun handleSceneAnalysisResponse(response: SceneAnalysisResponse) {
        when (response.sceneType) {
            "indoor_noisy", "outdoor_noisy" -> vibrationManager.vibrateStrong()
            "traffic" -> vibrationManager.vibrateForIntersection()
        }
        if (response.safetyLevel == "danger") {
            vibrationManager.vibrateEmergency()
        }
    }

    private suspend fun saveSceneRecord(
        response: SceneAnalysisResponse,
        audioLevel: Double,
        location: android.location.Location?
    ) {
        val vibrationPattern = when (response.safetyLevel) {
            "danger" -> 2
            "caution" -> 1
            else -> 0
        }
        val record = SceneRecordEntity(
            sceneType = response.sceneType,
            sceneSubtype = response.sceneType,
            description = response.recommendations.joinToString(","),
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            timestamp = response.timestamp,
            audioLevel = audioLevel,
            vibrationPattern = vibrationPattern,
            voiceEnabled = true
        )
        sceneRepository.saveRecord(record)
    }

    fun getCurrentSceneType(): String = currentSceneType

    fun triggerAnalysis() { analyzeScene() }
}

private fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)
    val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 85, out)
    return android.graphics.BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
        ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
}
