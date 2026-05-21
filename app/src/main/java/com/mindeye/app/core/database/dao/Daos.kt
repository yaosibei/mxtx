package com.mindeye.app.core.database.dao

import androidx.room.*
import com.mindeye.app.core.database.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据访问对象
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

/**
 * 场景记录数据访问对象
 */
@Dao
interface SceneRecordDao {

    @Query("SELECT * FROM scene_records ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentRecords(limit: Int = 50): List<SceneRecordEntity>

    @Query("SELECT * FROM scene_records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getRecordsByTimeRange(startTime: Long, endTime: Long): List<SceneRecordEntity>

    @Query("SELECT * FROM scene_records WHERE sceneType = :sceneType ORDER BY timestamp DESC")
    suspend fun getRecordsBySceneType(sceneType: String): List<SceneRecordEntity>

    @Insert
    suspend fun insertRecord(record: SceneRecordEntity)

    @Query("DELETE FROM scene_records WHERE timestamp < :thresholdTime")
    suspend fun deleteOldRecords(thresholdTime: Long)

    @Query("SELECT COUNT(*) FROM scene_records")
    fun getRecordCount(): Flow<Int>
}

/**
 * 聊天历史数据访问对象
 */
@Dao
interface ChatHistoryDao {

    @Query("SELECT * FROM chat_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentChats(limit: Int = 100): List<ChatHistoryEntity>

    @Query("SELECT * FROM chat_history WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getUserChatHistory(userId: String): List<ChatHistoryEntity>

    @Insert
    suspend fun insertChat(chat: ChatHistoryEntity)

    @Query("DELETE FROM chat_history WHERE timestamp < :thresholdTime")
    suspend fun deleteOldChats(thresholdTime: Long)

    @Query("DELETE FROM chat_history")
    suspend fun deleteAllChats()
}

/**
 * 帖子缓存数据访问对象
 */
@Dao
interface PostCacheDao {

    @Query("SELECT * FROM post_cache ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getCachedPosts(limit: Int = 50): List<PostCacheEntity>

    @Query("SELECT * FROM post_cache WHERE postId = :postId")
    suspend fun getPostById(postId: String): PostCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostCacheEntity)

    @Query("DELETE FROM post_cache WHERE cachedAt < :thresholdTime")
    suspend fun deleteOldPosts(thresholdTime: Long)

    @Query("UPDATE post_cache SET likeCount = likeCount + 1 WHERE postId = :postId")
    suspend fun incrementLikes(postId: String)

    @Query("UPDATE post_cache SET commentCount = commentCount + 1 WHERE postId = :postId")
    suspend fun incrementComments(postId: String)
}

/**
 * 紧急联系人数据访问对象
 */
@Dao
interface EmergencyContactDao {

    @Query("SELECT * FROM emergency_contacts ORDER BY sortOrder ASC")
    suspend fun getAllContacts(): List<EmergencyContactEntity>

    @Query("SELECT * FROM emergency_contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: String): EmergencyContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity)

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Query("SELECT * FROM emergency_contacts WHERE isDefault = 1")
    suspend fun getDefaultContacts(): List<EmergencyContactEntity>
}

/**
 * 评论数据访问对象
 */
@Dao
interface CommentDao {

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getCommentsByPostId(postId: String, limit: Int = 20): List<CommentEntity>

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCount(postId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: String)

    @Query("DELETE FROM comments WHERE createdAt < :thresholdTime")
    suspend fun deleteOldComments(thresholdTime: Long)
}

/**
 * 设置数据访问对象
 */
@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getSetting(key: String): SettingsEntity?

    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingsEntity)

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteSetting(key: String)

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsEntity>
}

/**
 * 求助请求数据访问对象
 */
@Dao
interface HelpRequestDao {

    @Query("SELECT * FROM help_requests WHERE status = 'PENDING' ORDER BY createdAt DESC LIMIT :limit")
    fun getPendingRequestsFlow(limit: Int = 50): kotlinx.coroutines.flow.Flow<List<HelpRequestEntity>>

    @Query("SELECT * FROM help_requests WHERE volunteerId = :volunteerId ORDER BY createdAt DESC")
    fun getVolunteerRequestsFlow(volunteerId: String): kotlinx.coroutines.flow.Flow<List<HelpRequestEntity>>

    @Query("SELECT * FROM help_requests WHERE status = 'PENDING' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getPendingRequests(limit: Int = 50): List<HelpRequestEntity>

    @Query("SELECT * FROM help_requests WHERE requesterId = :userId ORDER BY createdAt DESC")
    suspend fun getUserRequests(userId: String): List<HelpRequestEntity>

    @Query("SELECT * FROM help_requests WHERE volunteerId = :volunteerId ORDER BY createdAt DESC")
    suspend fun getVolunteerRequests(volunteerId: String): List<HelpRequestEntity>

    @Query("SELECT * FROM help_requests WHERE requestId = :requestId")
    suspend fun getRequestById(requestId: String): HelpRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: HelpRequestEntity)

    @Query("UPDATE help_requests SET status = :status, volunteerId = :volunteerId, volunteerName = :volunteerName, updatedAt = :updatedAt WHERE requestId = :requestId")
    suspend fun acceptRequest(requestId: String, status: String, volunteerId: String, volunteerName: String, updatedAt: Long)

    @Query("UPDATE help_requests SET status = :status, updatedAt = :updatedAt WHERE requestId = :requestId")
    suspend fun updateStatus(requestId: String, status: String, updatedAt: Long)

    @Query("DELETE FROM help_requests WHERE createdAt < :thresholdTime")
    suspend fun deleteOldRequests(thresholdTime: Long)
}

/**
 * AI问答数据访问对象
 */
@Dao
interface AiQaDao {

    @Query("SELECT * FROM ai_qa_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getUserQaHistory(userId: String, limit: Int = 50): List<AiQaEntity>

    @Query("SELECT * FROM ai_qa_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentQaHistory(limit: Int = 50): List<AiQaEntity>

    @Insert
    suspend fun insertQa(qa: AiQaEntity)

    @Query("DELETE FROM ai_qa_history WHERE timestamp < :thresholdTime")
    suspend fun deleteOldQa(thresholdTime: Long)
}

/**
 * 鼓励语数据访问对象
 */
@Dao
interface EncouragementDao {

    @Query("SELECT * FROM encouragements WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getUserEncouragements(userId: String, limit: Int = 20): List<EncouragementEntity>

    @Query("SELECT * FROM encouragements WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    suspend fun getUnreadEncouragements(userId: String): List<EncouragementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncouragement(encouragement: EncouragementEntity)

    @Query("UPDATE encouragements SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM encouragements WHERE createdAt < :thresholdTime")
    suspend fun deleteOldEncouragements(thresholdTime: Long)
}
