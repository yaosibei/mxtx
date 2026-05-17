package com.mindeye.app.core.network

import com.mindeye.app.core.model.ChatSupportRequest
import com.mindeye.app.core.model.ChatSupportResponse
import com.mindeye.app.core.model.SceneAnalysisRequest
import com.mindeye.app.core.model.SceneAnalysisResponse
import retrofit2.http.*

/**
 * 多模态 AI 服务接口
 * 用于环境分析和视觉识别
 */
interface MultimodalApiService {

    /**
     * 分析环境场景
     */
    @POST("v1/multimodal/analyze")
    suspend fun analyzeScene(@Body request: SceneAnalysisRequest): SceneAnalysisResponse

    /**
     * 识别物体
     */
    @POST("v1/multimodal/recognize")
    suspend fun recognizeObjects(@Body request: ObjectRecognitionRequest): ObjectRecognitionResponse

    /**
     * 文字识别
     */
    @POST("v1/multimodal/ocr")
    suspend fun recognizeText(@Body request: OCRRequest): OCRResponse

    /**
     * 人脸检测
     */
    @POST("v1/multimodal/face")
    suspend fun detectFaces(@Body request: FaceDetectionRequest): FaceDetectionResponse

    /**
     * 聊天支持
     */
    @POST("v1/multimodal/chat")
    suspend fun chatSupport(@Body request: ChatSupportRequest): ChatSupportResponse

    /**
     * 语音识别
     */
    @POST("v1/multimodal/speech")
    suspend fun recognizeSpeech(@Body request: SpeechRecognitionRequest): SpeechRecognitionResponse

    /**
     * 语音合成
     */
    @POST("v1/multimodal/tts")
    suspend fun textToSpeech(@Body request: TextToSpeechRequest): TextToSpeechResponse
}

/**
 * 物体识别请求
 */
data class ObjectRecognitionRequest(
    val imageData: String, // Base64 编码的图片数据
    val detectMode: String = "general" // general, food, person, vehicle
)

/**
 * 物体识别响应
 */
data class ObjectRecognitionResponse(
    val objects: List<RecognizedObject>,
    val confidence: Float
)

/**
 * 识别的物体
 */
data class RecognizedObject(
    val name: String,
    val confidence: Float,
    val boundingBox: BoundingBox? = null,
    val distance: Float? = null // 与用户的距离（米）
)

/**
 * 边界框
 */
data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

/**
 * OCR 请求
 */
data class OCRRequest(
    val imageData: String, // Base64 编码的图片数据
    val language: String = "auto" // 语言：auto, zh, en, ja, ko, fr, de, es
)

/**
 * OCR 响应
 */
data class OCRResponse(
    val text: String,
    val lines: List<OCRLine>,
    val confidence: Float
)

/**
 * OCR 行
 */
data class OCRLine(
    val text: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)

/**
 * 人脸检测请求
 */
data class FaceDetectionRequest(
    val imageData: String // Base64 编码的图片数据
)

/**
 * 人脸检测响应
 */
data class FaceDetectionResponse(
    val faces: List<DetectedFace>,
    val count: Int
)

/**
 * 检测到的人脸
 */
data class DetectedFace(
    val boundingBox: BoundingBox,
    val confidence: Float,
    val age: Int? = null,
    val gender: String? = null // male, female, unknown
)

/**
 * 语音识别请求
 */
data class SpeechRecognitionRequest(
    val audioData: String, // Base64 编码的音频数据
    val language: String = "zh-CN",
    val sampleRate: Int = 16000
)

/**
 * 语音识别响应
 */
data class SpeechRecognitionResponse(
    val text: String,
    val confidence: Float,
    val words: List<RecognizedWord>? = null
)

/**
 * 识别的单词
 */
data class RecognizedWord(
    val word: String,
    val start: Int, // 开始时间（毫秒）
    val end: Int, // 结束时间（毫秒）
    val confidence: Float
)

/**
 * 语音合成请求
 */
data class TextToSpeechRequest(
    val text: String,
    val voice: String = "zh-CN",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f
)

/**
 * 语音合成响应
 */
data class TextToSpeechResponse(
    val audioData: String, // Base64 编码的音频数据
    val duration: Int // 音频时长（毫秒）
)
