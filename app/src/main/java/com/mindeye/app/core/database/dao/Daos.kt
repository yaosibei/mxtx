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

    @Query("UPDATE post_cache SET comments = :commentsJson WHERE postId = :postId")
    suspend fun updateComments(postId: String, commentsJson: String)

    @Query("SELECT * FROM post_cache WHERE postType = 'HELP_REQUEST' ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getHelpRequests(limit: Int = 50): List<PostCacheEntity>

    @Query("SELECT * FROM post_cache WHERE postType != 'HELP_REQUEST' ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getRegularPosts(limit: Int = 50): List<PostCacheEntity>

    @Query("SELECT * FROM post_cache WHERE status = 'OPEN' AND postType = 'HELP_REQUEST' ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getOpenHelpRequests(limit: Int = 50): List<PostCacheEntity>

    @Query("SELECT * FROM post_cache WHERE authorId = :userId AND postType = 'HELP_REQUEST' ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getUserHelpRequests(userId: String, limit: Int = 50): List<PostCacheEntity>

    @Query("UPDATE post_cache SET status = 'ACCEPTED', volunteerName = :volunteerName, volunteerId = :volunteerId WHERE postId = :postId")
    suspend fun acceptHelpRequest(postId: String, volunteerName: String, volunteerId: String)

    @Query("UPDATE post_cache SET status = 'COMPLETED', completedAt = :completedAt WHERE postId = :postId")
    suspend fun completeHelpRequest(postId: String, completedAt: Long)

    @Query("UPDATE post_cache SET rating = :rating, feedback = :feedback WHERE postId = :postId")
    suspend fun rateHelpRequest(postId: String, rating: Int, feedback: String?)

    @Query("SELECT * FROM post_cache WHERE status = 'ACCEPTED' AND volunteerId = :volunteerId ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getVolunteerAcceptedRequests(volunteerId: String, limit: Int = 50): List<PostCacheEntity>

    @Query("SELECT * FROM post_cache WHERE helpCategory = :category AND postType = 'HELP_REQUEST' ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getHelpRequestsByCategory(category: String, limit: Int = 50): List<PostCacheEntity>

    @Query("SELECT COUNT(*) FROM post_cache WHERE postType = 'HELP_REQUEST' AND status = 'OPEN'")
    suspend fun getOpenHelpRequestCount(): Int

    @Query("SELECT COUNT(*) FROM post_cache WHERE authorId = :userId AND postType = 'HELP_REQUEST'")
    suspend fun getUserHelpRequestCount(userId: String): Int
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
 * 设置数据访问对象
 */
@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getSetting(key: String): SettingsEntity?

    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?

    @Query("SELECT value FROM settings WHERE key = :key")
    fun observeSettingValue(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingsEntity)

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteSetting(key: String)

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsEntity>
}

@Dao
interface TravelRecordDao {

    @Query("SELECT * FROM travel_records WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    suspend fun getUserRecords(userId: String, limit: Int = 50): List<TravelRecordEntity>

    @Query("SELECT * FROM travel_records WHERE recordId = :recordId")
    suspend fun getRecordById(recordId: String): TravelRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TravelRecordEntity)

    @Query("SELECT COUNT(*) FROM travel_records WHERE userId = :userId")
    suspend fun getUserRecordCount(userId: String): Int

    @Query("SELECT COALESCE(SUM(distance), 0) FROM travel_records WHERE userId = :userId AND isCompleted = 1")
    suspend fun getUserTotalDistance(userId: String): Double

    @Query("SELECT COALESCE(SUM(duration), 0) FROM travel_records WHERE userId = :userId AND isCompleted = 1")
    suspend fun getUserTotalDuration(userId: String): Long

    @Query("SELECT * FROM travel_records WHERE userId = :userId AND isCompleted = 1 ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestCompletedRecord(userId: String): TravelRecordEntity?
}

@Dao
interface VolunteerOrderDao {

    @Query("SELECT * FROM volunteer_orders WHERE volunteerId = :volunteerId ORDER BY createdAt DESC")
    suspend fun getVolunteerOrders(volunteerId: String): List<VolunteerOrderEntity>

    @Query("SELECT * FROM volunteer_orders WHERE requesterId = :requesterId ORDER BY createdAt DESC")
    suspend fun getRequesterOrders(requesterId: String): List<VolunteerOrderEntity>

    @Query("SELECT * FROM volunteer_orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): VolunteerOrderEntity?

    @Query("SELECT * FROM volunteer_orders WHERE postId = :postId LIMIT 1")
    suspend fun getOrderByPostId(postId: String): VolunteerOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: VolunteerOrderEntity)

    @Query("UPDATE volunteer_orders SET status = :status, acceptedAt = :acceptedAt WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, acceptedAt: Long)

    @Query("UPDATE volunteer_orders SET status = 'completed', completedAt = :completedAt WHERE orderId = :orderId")
    suspend fun completeOrder(orderId: String, completedAt: Long)

    @Query("UPDATE volunteer_orders SET rating = :rating, feedback = :feedback WHERE orderId = :orderId")
    suspend fun rateOrder(orderId: String, rating: Int, feedback: String?)

    @Query("SELECT COUNT(*) FROM volunteer_orders WHERE volunteerId = :volunteerId AND status = 'completed'")
    suspend fun getCompletedOrderCount(volunteerId: String): Int

    @Query("SELECT COUNT(*) FROM volunteer_orders WHERE volunteerId = :volunteerId AND status = 'accepted'")
    suspend fun getPendingOrderCount(volunteerId: String): Int

    @Query("SELECT COALESCE(AVG(rating), 5.0) FROM volunteer_orders WHERE volunteerId = :volunteerId AND rating > 0")
    suspend fun getAverageRating(volunteerId: String): Double

    @Query("SELECT * FROM volunteer_orders WHERE status = 'pending' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getPendingOrders(limit: Int = 50): List<VolunteerOrderEntity>
}

@Dao
interface MoodRecordDao {

    @Query("SELECT * FROM mood_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getUserMoodRecords(userId: String, limit: Int = 50): List<MoodRecordEntity>

    @Query("SELECT * FROM mood_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMood(userId: String): MoodRecordEntity?

    @Insert
    suspend fun insertMoodRecord(record: MoodRecordEntity)

    @Query("SELECT mood FROM mood_records WHERE userId = :userId GROUP BY mood ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostFrequentMood(userId: String): String?

    @Query("SELECT * FROM mood_records WHERE userId = :userId AND timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getMoodRecordsInRange(userId: String, start: Long, end: Long): List<MoodRecordEntity>
}

@Dao
interface HelpHistoryDao {

    @Query("SELECT * FROM help_history WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getUserHelpHistory(userId: String, limit: Int = 50): List<HelpHistoryEntity>

    @Query("SELECT * FROM help_history WHERE helperId = :helperId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getHelperHistory(helperId: String, limit: Int = 50): List<HelpHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHelpHistory(history: HelpHistoryEntity)

    @Query("UPDATE help_history SET rating = :rating, feedback = :feedback, completedAt = :completedAt WHERE historyId = :historyId")
    suspend fun updateHelpRating(historyId: String, rating: Int, feedback: String?, completedAt: Long)

    @Query("SELECT COUNT(*) FROM help_history WHERE helperId = :helperId")
    suspend fun getHelperCompletedCount(helperId: String): Int
}

@Dao
interface UserBadgeDao {

    @Query("SELECT * FROM user_badges WHERE userId = :userId ORDER BY unlockedAt DESC")
    suspend fun getUserBadges(userId: String): List<UserBadgeEntity>

    @Query("SELECT * FROM user_badges WHERE badgeId = :badgeId")
    suspend fun getBadgeById(badgeId: String): UserBadgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: UserBadgeEntity)

    @Query("SELECT COUNT(*) FROM user_badges WHERE userId = :userId")
    suspend fun getUserBadgeCount(userId: String): Int

    @Query("SELECT * FROM user_badges WHERE userId = :userId AND badgeKey = :badgeKey LIMIT 1")
    suspend fun getBadgeByKey(userId: String, badgeKey: String): UserBadgeEntity?
}

@Dao
interface VolunteerProfileDao {

    @Query("SELECT * FROM volunteer_profiles WHERE volunteerId = :volunteerId")
    suspend fun getProfileById(volunteerId: String): VolunteerProfileEntity?

    @Query("SELECT * FROM volunteer_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfileByUserId(userId: String): VolunteerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VolunteerProfileEntity)

    @Query("UPDATE volunteer_profiles SET serviceCount = :count, completedOrders = :completed, rating = :rating, totalHours = :hours, level = :level, updatedAt = :updatedAt WHERE volunteerId = :volunteerId")
    suspend fun updateStats(volunteerId: String, count: Int, completed: Int, rating: Double, hours: Long, level: String, updatedAt: Long)

    @Query("SELECT * FROM volunteer_profiles WHERE isActive = 1 ORDER BY rating DESC LIMIT :limit")
    suspend fun getTopVolunteers(limit: Int = 10): List<VolunteerProfileEntity>
}

@Dao
interface HelpRequestSyncDao {

    @Query("SELECT * FROM help_request_sync WHERE status = 'pending' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getPendingSyncs(limit: Int = 50): List<HelpRequestSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSync(sync: HelpRequestSyncEntity)

    @Query("UPDATE help_request_sync SET status = :status, syncedAt = :syncedAt WHERE syncId = :syncId")
    suspend fun markSynced(syncId: String, status: String, syncedAt: Long)

    @Query("UPDATE help_request_sync SET retryCount = retryCount + 1 WHERE syncId = :syncId")
    suspend fun incrementRetry(syncId: String)

    @Query("DELETE FROM help_request_sync WHERE syncedAt < :thresholdTime")
    suspend fun deleteOldSyncs(thresholdTime: Long)
}
