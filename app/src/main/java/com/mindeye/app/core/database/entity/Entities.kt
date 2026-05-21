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
 * 设置实体
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)
