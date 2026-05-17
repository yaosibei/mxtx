package com.mindeye.app.core.model

/**
 * 场景分析请求
 */
data class SceneAnalysisRequest(
    val imageData: String, // Base64 编码的图片数据
    val audioLevel: Float, // 音频分贝级别
    val locationData: LocationData? = null, // 位置数据
    val sensorData: SensorData? = null // 传感器数据
)

/**
 * 场景分析响应
 */
data class SceneAnalysisResponse(
    val sceneType: String, // 场景类型：indoor_quiet, indoor_noisy, outdoor_quiet, outdoor_noisy, social, traffic
    val confidence: Float, // 置信度
    val recommendations: List<String>, // 建议
    val safetyLevel: String, // 安全级别：safe, caution, danger
    val timestamp: Long // 时间戳
)

/**
 * 位置数据
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null
)

/**
 * 传感器数据
 */
data class SensorData(
    val accelerometer: FloatArray? = null, // 加速度
    val gyroscope: FloatArray? = null, // 陀螺仪
    val light: Float? = null, // 光线强度
    val proximity: Float? = null // 距离传感器
)

/**
 * 聊天支持请求
 */
data class ChatSupportRequest(
    val userId: String,
    val message: String,
    val mood: String? = null,
    val context: String? = null
)

/**
 * 聊天支持响应
 */
data class ChatSupportResponse(
    val reply: String,
    val mood: String? = null,
    val confidence: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 帖子项
 */
data class PostItem(
    val id: String,
    val userId: String,
    val userName: String,
    val content: String,
    val type: String, // text, voice, image
    val likes: Int = 0,
    val comments: Int = 0,
    val timestamp: Long,
    val location: String? = null
)
