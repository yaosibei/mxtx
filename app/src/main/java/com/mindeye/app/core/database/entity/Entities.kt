package com.mindeye.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    val avatar: String?,
    val phone: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val preferences: String?  // JSON 格式的用户偏好设置
)

/**
 * 场景记录实体
 */
@Entity(tableName = "scene_records")
data class SceneRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sceneType: String,          // indoor/outdoor/social/transit
    val sceneSubtype: String,       // quiet/noisy
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long,
    val audioLevel: Double,
    val vibrationPattern: Int,
    val voiceEnabled: Boolean
)

/**
 * 聊天历史实体
 */
@Entity(tableName = "chat_history")
data class ChatHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val message: String,
    val reply: String,
    val mood: String?,
    val timestamp: Long,
    val isFromUser: Boolean
)

/**
 * 帖子缓存实体
 */
@Entity(tableName = "post_cache")
data class PostCacheEntity(
    @PrimaryKey val postId: String,
    val title: String = "",
    val content: String,
    val authorId: String,
    val authorNickname: String,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val cachedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

/**
 * 紧急联系人实体
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)

/**
 * 评论实体
 */
@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val commentId: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 求助请求实体
 */
@Entity(tableName = "help_requests")
data class HelpRequestEntity(
    @PrimaryKey val requestId: String,
    val requesterId: String,
    val requesterName: String,
    val content: String,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String = "PENDING",  // PENDING, ACCEPTED, COMPLETED, CANCELLED
    val volunteerId: String? = null,
    val volunteerName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): com.mindeye.app.core.model.HelpRequest {
        return com.mindeye.app.core.model.HelpRequest(
            id = requestId,
            requesterId = requesterId,
            requesterName = requesterName,
            content = content,
            location = location,
            latitude = latitude,
            longitude = longitude,
            status = when (status) {
                "ACCEPTED" -> com.mindeye.app.core.model.HelpRequestStatus.ACCEPTED
                "COMPLETED" -> com.mindeye.app.core.model.HelpRequestStatus.COMPLETED
                "CANCELLED" -> com.mindeye.app.core.model.HelpRequestStatus.CANCELLED
                else -> com.mindeye.app.core.model.HelpRequestStatus.PENDING
            },
            volunteerId = volunteerId,
            volunteerName = volunteerName,
            timestamp = createdAt
        )
    }
}

/**
 * AI问答记录实体
 */
@Entity(tableName = "ai_qa_history")
data class AiQaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 鼓励语实体
 */
@Entity(tableName = "encouragements")
data class EncouragementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val content: String,
    val triggerScene: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 设置实体
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
