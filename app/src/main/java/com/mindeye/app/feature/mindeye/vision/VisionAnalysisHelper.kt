package com.mindeye.app.feature.mindeye.vision

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.mindeye.app.feature.mindeye.speech.XunfeiSpeechManager
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

object VisionAnalysisHelper {

    private const val TAG = "VisionAnalysisHelper"
    private const val SAME_OBJECT_INTERVAL_MS = 5_000L

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: androidx.camera.core.Preview? = null
    private var analysisExecutor: ExecutorService? = null
    private var objectDetector: ObjectDetector? = null

    private val lastAnnounceMap = mutableMapOf<String, Long>()
    private val isProcessingFrame = AtomicBoolean(false)
    private val isReleased = AtomicBoolean(false)

    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: androidx.camera.core.Preview.SurfaceProvider? = null
    ) {
        val appContext = context.applicationContext
        isReleased.set(false)
        if (analysisExecutor == null || analysisExecutor?.isShutdown == true) {
            analysisExecutor = Executors.newSingleThreadExecutor()
        }
        if (objectDetector == null) {
            objectDetector = createObjectDetector()
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(appContext)
        cameraProviderFuture.addListener(
            {
                try {
                    cameraProvider = cameraProviderFuture.get()
                    
                    val useCases = mutableListOf<androidx.camera.core.UseCase>()

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { useCase ->
                            useCase.setAnalyzer(
                                analysisExecutor ?: Executors.newSingleThreadExecutor(),
                                FrameAnalyzer()
                            )
                        }
                    imageAnalysis = analysis
                    useCases.add(analysis)

                    if (surfaceProvider != null) {
                        val previewUseCase = androidx.camera.core.Preview.Builder().build().also {
                            it.setSurfaceProvider(surfaceProvider)
                        }
                        preview = previewUseCase
                        useCases.add(previewUseCase)
                    }

                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        *useCases.toTypedArray()
                    )
                    Log.d(TAG, "CameraX 已启动，绑定了 ${useCases.size} 个用例")
                } catch (e: Exception) {
                    Log.e(TAG, "启动 CameraX 失败: ${e.message}", e)
                }
            },
            ContextCompat.getMainExecutor(appContext)
        )
    }

    fun stopCamera() {
        try {
            isProcessingFrame.set(false)
            imageAnalysis?.clearAnalyzer()
            cameraProvider?.unbindAll()
            imageAnalysis = null
            preview = null
            lastAnnounceMap.clear()
        } catch (e: Exception) {
            Log.e(TAG, "停止 CameraX 失败: ${e.message}", e)
        }
    }

    fun release() {
        isReleased.set(true)
        stopCamera()
        objectDetector?.close()
        objectDetector = null
        analysisExecutor?.shutdown()
        analysisExecutor = null
    }

    private fun createObjectDetector(): ObjectDetector {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        return ObjectDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    private class FrameAnalyzer : ImageAnalysis.Analyzer {

        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            val detector = objectDetector
            if (isReleased.get() || mediaImage == null || detector == null) {
                imageProxy.close()
                return
            }

            if (!isProcessingFrame.compareAndSet(false, true)) {
                imageProxy.close()
                return
            }

            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            detector.process(inputImage)
                .addOnSuccessListener { detectedObjects ->
                    if (!isReleased.get()) {
                        handleDetectedObjects(detectedObjects)
                    }
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "ML Kit 物体检测失败: ${error.message}", error)
                }
                .addOnCompleteListener {
                    isProcessingFrame.set(false)
                    imageProxy.close()
                }
        }

        private fun handleDetectedObjects(detectedObjects: List<DetectedObject>) {
            detectedObjects.forEach { detectedObject ->
                val objectName = resolveObjectName(detectedObject)
                if (shouldAnnounce(objectName)) {
                    lastAnnounceMap[objectName] = SystemClock.elapsedRealtime()
                    XunfeiSpeechManager.speak("前方发现$objectName，请注意安全")
                }
            }
        }

        private fun resolveObjectName(detectedObject: DetectedObject): String {
            val label = detectedObject.labels
                .maxByOrNull { it.confidence }
                ?.text
                ?.trim()
                .orEmpty()

            if (label.isBlank()) {
                return "障碍物"
            }

            return when (label.lowercase(Locale.getDefault())) {
                "door" -> "门"
                "stairs", "stair" -> "楼梯"
                "person", "people" -> "行人"
                "chair" -> "椅子"
                "table", "desk" -> "桌子"
                "car", "vehicle" -> "车辆"
                "bus" -> "公交车"
                "truck" -> "卡车"
                "bicycle", "bike" -> "自行车"
                "motorcycle" -> "摩托车"
                "dog" -> "狗"
                "cat" -> "猫"
                else -> label
            }
        }

        private fun shouldAnnounce(objectName: String): Boolean {
            val currentTime = SystemClock.elapsedRealtime()
            val lastTime = lastAnnounceMap[objectName] ?: 0L
            return currentTime - lastTime >= SAME_OBJECT_INTERVAL_MS
        }
    }
}
