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
    val location: String? = null
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

data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)
