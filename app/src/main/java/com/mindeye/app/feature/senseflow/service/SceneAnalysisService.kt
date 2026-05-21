package com.mindeye.app.feature.senseflow.service

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.mindeye.app.core.database.AppDatabase
import com.mindeye.app.core.database.entity.SceneRecordEntity
import com.mindeye.app.feature.senseflow.data.SceneRepository
import com.mindeye.app.feature.senseflow.domain.*
import com.mindeye.app.core.sensor.AudioLevelListener
import com.mindeye.app.core.sensor.AppSensorManager
import com.mindeye.app.core.feedback.VibrationManager
import com.mindeye.app.core.location.LocationService
import com.mindeye.app.feature.mindeye.speech.XunfeiSpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 随境 SenseFlow 场景分析服务。
 * 启动后在后台持续运行，通过传感器+音频+摄像头判断场景，
 * 根据场景类型自动选择语音/震动策略。
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
    private var currentState: SenseFlowState = SenseFlowState()
    private val handler = Handler(Looper.getMainLooper())
    private val analysisInterval = 3000L

    private var capturedBitmap: Bitmap? = null

    /** 场景分析结果回调，供 UI 层监听 */
    var onSceneChanged: ((SenseFlowState) -> Unit)? = null

    fun start() {
        if (isRunning) return
        isRunning = true
        sensorManager.startListening()
        audioLevelListener.startListening()
        initializeCamera()
        startAnalysisLoop()
        XunfeiSpeechManager.speak("场景分析已启动")
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
        XunfeiSpeechManager.speak("场景分析已停止")
        Log.d(TAG, "Scene analysis service stopped")
    }

    fun release() {
        stop()
        audioLevelListener.release()
        sensorManager.release()
        vibrationManager.cancel()
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

                // 本地快速判断
                val localState = analyzeSceneLocally(audioLevel, isMoving, motionState)

                // 有图片数据时尝试云端分析
                val response = if (imageData.isNotEmpty()) {
                    try {
                        val cloudState = analyzeSceneWithCloud(imageData, audioLevel, location)
                        cloudState
                    } catch (e: Exception) {
                        Log.w(TAG, "云端分析失败，使用本地结果: ${e.message}")
                        localState
                    }
                } else {
                    localState
                }

                // 场景变化检测
                if (response.sceneType != currentState.sceneType) {
                    onSceneAnnounce(response.sceneType)
                }

                currentState = response
                onSceneChanged?.invoke(response)

                // 执行反馈策略
                applyFeedbackStrategy(response)

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
        return ""
    }

    private fun analyzeSceneLocally(
        audioLevel: Float,
        isMoving: Boolean,
        motionState: String
    ): SenseFlowState {
        val sceneType = when {
            audioLevel < 40f && !isMoving -> SenseSceneType.INDOOR_QUIET
            audioLevel > 65f && !isMoving -> SenseSceneType.INDOOR_NOISY
            audioLevel < 50f && isMoving -> SenseSceneType.OUTDOOR_QUIET
            audioLevel > 70f && isMoving -> SenseSceneType.OUTDOOR_NOISY
            audioLevel in 50f..70f && motionState == "walking" -> SenseSceneType.SOCIAL
            audioLevel > 60f && motionState == "vehicle" -> SenseSceneType.TRAFFIC
            else -> SenseSceneType.INDOOR_QUIET
        }

        val noiseLevel = when {
            audioLevel < 40f -> NoiseLevel.QUIET
            audioLevel < 65f -> NoiseLevel.NORMAL
            else -> NoiseLevel.NOISY
        }

        val motion = when (motionState) {
            "still" -> MotionState.STILL
            "walking" -> MotionState.WALKING
            "running" -> MotionState.RUNNING
            "vehicle" -> MotionState.VEHICLE
            else -> MotionState.UNKNOWN
        }

        val strategy = decideFeedbackStrategy(sceneType, noiseLevel)

        return SenseFlowState(
            sceneType = sceneType,
            noiseLevel = noiseLevel,
            motionState = motion,
            feedbackStrategy = strategy,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 云端场景分析（需要 MultimodalApiService 实现）。
     * 由于云端服务可能未部署，此处保留接口，默认回退本地分析。
     */
    private fun analyzeSceneWithCloud(
        imageData: String,
        audioLevel: Float,
        location: android.location.Location?
    ): SenseFlowState {
        // TODO: 接入 MultimodalApiService.analyzeScene()
        // 云端服务未就绪时，使用本地分析结果
        return analyzeSceneLocally(
            audioLevel = audioLevel,
            isMoving = sensorManager.isMoving(),
            motionState = sensorManager.detectMotionState()
        )
    }

    /**
     * 根据场景类型决定反馈策略。
     */
    private fun decideFeedbackStrategy(
        sceneType: SenseSceneType,
        noiseLevel: NoiseLevel
    ): FeedbackStrategy = when (sceneType) {
        SenseSceneType.INDOOR_QUIET -> FeedbackStrategy.VOICE_MAIN
        SenseSceneType.INDOOR_NOISY -> FeedbackStrategy.SHORT_VOICE_WITH_VIBRATION
        SenseSceneType.OUTDOOR_QUIET -> FeedbackStrategy.VOICE_MAIN
        SenseSceneType.OUTDOOR_NOISY -> FeedbackStrategy.VIBRATION_MAIN
        SenseSceneType.SOCIAL -> FeedbackStrategy.SHORT_VOICE_WITH_VIBRATION
        SenseSceneType.TRAFFIC -> FeedbackStrategy.EMERGENCY_INTERRUPT
        SenseSceneType.UNKNOWN -> FeedbackStrategy.VOICE_MAIN
    }

    /**
     * 执行当前反馈策略。
     */
    private fun applyFeedbackStrategy(state: SenseFlowState) {
        when (state.feedbackStrategy) {
            FeedbackStrategy.VOICE_MAIN -> {
                // 语音为主，仅轻微震动确认
            }
            FeedbackStrategy.VIBRATION_MAIN -> {
                vibrationManager.vibrateStrong()
            }
            FeedbackStrategy.SHORT_VOICE_WITH_VIBRATION -> {
                vibrationManager.vibrateNormal()
            }
            FeedbackStrategy.SILENT_SCREEN -> {
                // 静默模式
            }
            FeedbackStrategy.EMERGENCY_INTERRUPT -> {
                vibrationManager.vibrateEmergency()
            }
        }

        // 根据场景类型播报语音提示
        val announcement = getSceneAnnouncement(state.sceneType)
        if (announcement.isNotEmpty()) {
            XunfeiSpeechManager.speak(announcement)
        }
    }

    /**
     * 场景变化时播报。
     */
    private fun onSceneAnnounce(sceneType: SenseSceneType) {
        val text = when (sceneType) {
            SenseSceneType.INDOOR_QUIET -> "已进入安静室内环境"
            SenseSceneType.INDOOR_NOISY -> "室内环境较嘈杂，建议使用震动反馈"
            SenseSceneType.OUTDOOR_QUIET -> "已进入安静户外环境"
            SenseSceneType.OUTDOOR_NOISY -> "户外环境较嘈杂，建议使用震动导航"
            SenseSceneType.SOCIAL -> "检测到周围有人群活动"
            SenseSceneType.TRAFFIC -> "检测到交通环境，请注意交通安全"
            SenseSceneType.UNKNOWN -> ""
        }
        if (text.isNotEmpty()) {
            XunfeiSpeechManager.speak(text)
        }
    }

    private fun getSceneAnnouncement(sceneType: SenseSceneType): String = when (sceneType) {
        SenseSceneType.TRAFFIC -> "注意交通安全"
        SenseSceneType.SOCIAL -> "注意周围人流"
        SenseSceneType.OUTDOOR_NOISY -> "建议使用震动导航"
        SenseSceneType.INDOOR_NOISY -> "建议使用震动反馈"
        else -> ""
    }

    private suspend fun saveSceneRecord(
        state: SenseFlowState,
        audioLevel: Double,
        location: android.location.Location?
    ) {
        val vibrationPattern = when (state.feedbackStrategy) {
            FeedbackStrategy.EMERGENCY_INTERRUPT -> 2
            FeedbackStrategy.VIBRATION_MAIN, FeedbackStrategy.SHORT_VOICE_WITH_VIBRATION -> 1
            else -> 0
        }
        val record = SceneRecordEntity(
            sceneType = state.sceneType.name.lowercase(),
            sceneSubtype = state.sceneType.name.lowercase(),
            description = "${state.noiseLevel.name}, ${state.motionState.name}",
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            timestamp = state.timestamp,
            audioLevel = audioLevel,
            vibrationPattern = vibrationPattern,
            voiceEnabled = state.feedbackStrategy != FeedbackStrategy.SILENT_SCREEN
        )
        sceneRepository.saveRecord(record)
    }

    fun getCurrentSceneType(): SenseSceneType = currentState.sceneType

    fun getCurrentState(): SenseFlowState = currentState

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
