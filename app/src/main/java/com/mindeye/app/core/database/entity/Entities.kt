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
    val role: String = "user",
    val createdAt: Long,
    val updatedAt: Long,
    val preferences: String?
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
    val postType: String = "TEXT",
    val status: String = "OPEN",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val cachedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val volunteerName: String? = null,
    val volunteerId: String? = null,
    val helpCategory: String = "OTHER",
    val urgencyLevel: String = "LOW",
    val comments: String = "[]",
    val rating: Int = 0,
    val feedback: String? = null,
    val completedAt: Long? = null
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

@Entity(tableName = "travel_records")
data class TravelRecordEntity(
    @PrimaryKey val recordId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long?,
    val startLocation: String?,
    val endLocation: String?,
    val startLatitude: Double?,
    val startLongitude: Double?,
    val endLatitude: Double?,
    val endLongitude: Double?,
    val distance: Double?,
    val duration: Long?,
    val sceneRecords: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "volunteer_orders")
data class VolunteerOrderEntity(
    @PrimaryKey val orderId: String,
    val postId: String,
    val requesterId: String,
    val requesterName: String,
    val volunteerId: String,
    val volunteerName: String,
    val status: String = "pending",
    val content: String,
    val helpCategory: String = "OTHER",
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long?,
    val completedAt: Long?,
    val rating: Int = 0,
    val feedback: String? = null,
    val isSynced: Boolean = false
)

@Entity(tableName = "mood_records")
data class MoodRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val mood: String,
    val moodLabel: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

@Entity(tableName = "help_history")
data class HelpHistoryEntity(
    @PrimaryKey val historyId: String,
    val userId: String,
    val postId: String,
    val helperId: String,
    val helperName: String,
    val helpCategory: String,
    val content: String,
    val rating: Int = 0,
    val feedback: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "user_badges")
data class UserBadgeEntity(
    @PrimaryKey val badgeId: String,
    val userId: String,
    val badgeKey: String,
    val badgeName: String,
    val badgeIcon: String,
    val badgeDescription: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "volunteer_profiles")
data class VolunteerProfileEntity(
    @PrimaryKey val volunteerId: String,
    val userId: String,
    val name: String,
    val avatar: String? = null,
    val serviceCount: Int = 0,
    val completedOrders: Int = 0,
    val rating: Double = 5.0,
    val totalHours: Long = 0,
    val joinDate: Long = System.currentTimeMillis(),
    val level: String = "NEWBIE",
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "help_request_sync")
data class HelpRequestSyncEntity(
    @PrimaryKey val syncId: String,
    val postId: String,
    val operation: String,
    val status: String = "pending",
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
