package com.mindeye.app.core.model

data class SceneAnalysisResult(
    val sceneType: SceneType,
    val confidence: Float,
    val recommendations: List<String>,
    val safetyLevel: SafetyLevel,
    val timestamp: Long,
    val recognizedText: String? = null,
    val detectedObjects: List<String> = emptyList()
)

enum class SceneType {
    INDOOR_QUIET,
    INDOOR_NOISY,
    OUTDOOR_QUIET,
    OUTDOOR_NOISY,
    SOCIAL,
    TRAFFIC,
    UNKNOWN
}

enum class SafetyLevel {
    SAFE,
    CAUTION,
    DANGER
}

data class ChatMessage(
    val id: Long = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String? = null
)

data class CommunityPost(
    val id: String,
    val userId: String,
    val userName: String,
    val content: String,
    val postType: PostType = PostType.TEXT,
    val likes: Int = 0,
    val comments: Int = 0,
    val timestamp: Long,
    val location: String? = null,
    val commentList: List<Comment> = emptyList()
)

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val content: String,
    val timestamp: Long
)

enum class PostType {
    TEXT,
    VOICE,
    IMAGE
}

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val isDefault: Boolean = false
)

data class VoiceCommand(
    val text: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class OcrResult(
    val text: String,
    val blocks: List<TextBlock> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class TextBlock(
    val text: String,
    val confidence: Float? = null,
    val boundingBox: Rect? = null
)

/**
 * 求助请求状态
 */
enum class HelpRequestStatus {
    PENDING,    // 待接单
    ACCEPTED,   // 已接单
    COMPLETED,  // 已完成
    CANCELLED   // 已取消
}

/**
 * 求助请求
 */
data class HelpRequest(
    val id: String,
    val requesterId: String,
    val requesterName: String,
    val content: String,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: HelpRequestStatus = HelpRequestStatus.PENDING,
    val volunteerId: String? = null,
    val volunteerName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AI问答消息
 */
data class AiQaMessage(
    val id: Long = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 鼓励语
 */
data class Encouragement(
    val id: String,
    val content: String,
    val triggerScene: String,  // 触发场景：indoor_quiet, travel_completed等
    val timestamp: Long = System.currentTimeMillis()
)

data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)
