package com.mindeye.app.feature.community.data

import com.mindeye.app.core.model.*
import com.mindeye.app.core.database.dao.*
import com.mindeye.app.core.database.entity.*
import com.mindeye.app.core.network.CommunityApiService
import com.mindeye.app.core.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postCacheDao: PostCacheDao,
    private val volunteerOrderDao: VolunteerOrderDao,
    private val volunteerProfileDao: VolunteerProfileDao,
    private val helpHistoryDao: HelpHistoryDao,
    private val userBadgeDao: UserBadgeDao,
    private val helpRequestSyncDao: HelpRequestSyncDao,
    private val apiService: CommunityApiService? = null
) {

    suspend fun getCachedPosts(limit: Int = 50): List<CommunityPost> {
        return postCacheDao.getRegularPosts(limit).map { it.toCommunityPost() }
    }

    suspend fun getHelpRequests(limit: Int = 50): List<CommunityPost> {
        return postCacheDao.getHelpRequests(limit).map { it.toCommunityPost() }
    }

    suspend fun getRegularPosts(limit: Int = 50): List<CommunityPost> {
        return postCacheDao.getRegularPosts(limit).map { it.toCommunityPost() }
    }

    suspend fun getOpenHelpRequests(limit: Int = 50): List<CommunityPost> {
        return postCacheDao.getOpenHelpRequests(limit).map { it.toCommunityPost() }
    }

    suspend fun getHelpRequestsByCategory(category: HelpCategory): List<CommunityPost> {
        return postCacheDao.getHelpRequestsByCategory(category.name).map { it.toCommunityPost() }
    }

    suspend fun getUserHelpRequests(userId: String): List<CommunityPost> {
        return postCacheDao.getUserHelpRequests(userId).map { it.toCommunityPost() }
    }

    suspend fun getPostDetail(postId: String): CommunityPost? {
        return postCacheDao.getPostById(postId)?.toCommunityPost()
    }

    suspend fun createPost(userId: String, userName: String, content: String, postType: PostType): Boolean {
        return try {
            val postId = System.currentTimeMillis().toString()
            postCacheDao.insertPost(PostCacheEntity(
                postId = postId,
                authorId = userId,
                authorNickname = userName,
                content = content,
                postType = postType.name,
                createdAt = System.currentTimeMillis()
            ))
            true
        } catch (e: Exception) { false }
    }

    suspend fun createHelpRequest(
        userId: String,
        userName: String,
        content: String,
        helpCategory: HelpCategory = HelpCategory.OTHER,
        urgencyLevel: UrgencyLevel = UrgencyLevel.LOW,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null
    ): Boolean {
        return try {
            val postId = System.currentTimeMillis().toString()
            postCacheDao.insertPost(PostCacheEntity(
                postId = postId,
                authorId = userId,
                authorNickname = userName,
                content = content,
                postType = PostType.HELP_REQUEST.name,
                status = PostStatus.OPEN.name,
                createdAt = System.currentTimeMillis(),
                latitude = latitude,
                longitude = longitude,
                locationName = locationName,
                helpCategory = helpCategory.name,
                urgencyLevel = urgencyLevel.name
            ))

            runCatching {
                helpRequestSyncDao.insertSync(HelpRequestSyncEntity(
                    syncId = "sync_$postId",
                    postId = postId,
                    operation = "CREATE_HELP_REQUEST"
                ))
            }

            runCatching {
                apiService?.createHelpRequest(CreateHelpRequest(
                    userId = userId,
                    userName = userName,
                    content = content,
                    helpCategory = helpCategory.name,
                    urgencyLevel = urgencyLevel.name,
                    latitude = latitude,
                    longitude = longitude,
                    locationName = locationName
                ))
            }

            true
        } catch (e: Exception) { false }
    }

    suspend fun likePost(postId: String): Boolean {
        return try { postCacheDao.incrementLikes(postId); true } catch (e: Exception) { false }
    }

    suspend fun addComment(postId: String, authorName: String, content: String, isFromVolunteer: Boolean = false): Boolean {
        return try {
            val post = postCacheDao.getPostById(postId) ?: return false
            val commentsJson = post.comments?.takeIf { it.isNotEmpty() } ?: "[]"
            val arr = JSONArray(commentsJson)
            val newComment = JSONObject().apply {
                put("id", System.currentTimeMillis().toString())
                put("authorName", authorName)
                put("content", content)
                put("timestamp", System.currentTimeMillis())
                put("isFromVolunteer", isFromVolunteer)
            }
            arr.put(newComment)
            postCacheDao.updateComments(postId, arr.toString())
            true
        } catch (e: Exception) {
            android.util.Log.e("PostRepository", "addComment error: ${e.message}", e)
            false
        }
    }

    suspend fun acceptHelpRequest(postId: String, volunteerId: String, volunteerName: String): Boolean {
        return try {
            val post = postCacheDao.getPostById(postId) ?: return false
            postCacheDao.acceptHelpRequest(postId, volunteerName, volunteerId)
            volunteerOrderDao.insertOrder(VolunteerOrderEntity(
                orderId = "order_${System.currentTimeMillis()}",
                postId = postId,
                requesterId = post.authorId,
                requesterName = post.authorNickname,
                volunteerId = volunteerId,
                volunteerName = volunteerName,
                status = "accepted",
                content = post.content,
                helpCategory = post.helpCategory,
                latitude = post.latitude,
                longitude = post.longitude,
                acceptedAt = System.currentTimeMillis(),
                completedAt = null
            ))

            try {
                apiService?.acceptHelpRequest(postId, AcceptHelpRequest(volunteerId, volunteerName))
            } catch (_: Exception) {}

            true
        } catch (e: Exception) { false }
    }

    suspend fun completeHelpRequest(postId: String, volunteerId: String? = null): Boolean {
        return try {
            val completedAt = System.currentTimeMillis()
            postCacheDao.completeHelpRequest(postId, completedAt)
            if (volunteerId != null) {
                volunteerOrderDao.getVolunteerOrders(volunteerId).firstOrNull { it.postId == postId && it.status == "accepted" }
                    ?.let { order ->
                        volunteerOrderDao.completeOrder(order.orderId, completedAt)
                        helpHistoryDao.insertHelpHistory(HelpHistoryEntity(
                            historyId = "hist_${order.orderId}",
                            userId = order.requesterId,
                            postId = postId,
                            helperId = volunteerId,
                            helperName = order.volunteerName,
                            helpCategory = order.helpCategory,
                            content = order.content,
                            createdAt = order.createdAt,
                            completedAt = completedAt
                        ))
                    }
            }

            try {
                apiService?.completeHelpRequest(postId, CompleteHelpRequest(volunteerId ?: ""))
            } catch (_: Exception) {}

            true
        } catch (e: Exception) { false }
    }

    suspend fun rateHelpRequest(postId: String, orderId: String, userId: String, rating: Int, feedback: String?): Boolean {
        return try {
            postCacheDao.rateHelpRequest(postId, rating, feedback)
            volunteerOrderDao.rateOrder(orderId, rating, feedback)
            true
        } catch (e: Exception) { false }
    }

    suspend fun getVolunteerOrderHistory(volunteerId: String): List<VolunteerOrderEntity> {
        return volunteerOrderDao.getVolunteerOrders(volunteerId)
    }

    suspend fun getRequesterOrderHistory(requesterId: String): List<VolunteerOrderEntity> {
        return volunteerOrderDao.getRequesterOrders(requesterId)
    }

    suspend fun getVolunteerStats(volunteerId: String): VolunteerStats {
        val completed = volunteerOrderDao.getCompletedOrderCount(volunteerId)
        val pending = volunteerOrderDao.getPendingOrderCount(volunteerId)
        val avgRating = volunteerOrderDao.getAverageRating(volunteerId)
        return VolunteerStats(
            totalServices = completed + pending,
            completedOrders = completed,
            pendingOrders = pending,
            totalHours = completed * 2L,
            rating = avgRating
        )
    }

    suspend fun syncPendingRequests() {
        val pending = helpRequestSyncDao.getPendingSyncs()
        for (sync in pending) {
            try {
                when (sync.operation) {
                    "CREATE_HELP_REQUEST" -> {
                        val post = postCacheDao.getPostById(sync.postId) ?: continue
                        apiService?.createHelpRequest(CreateHelpRequest(
                            userId = post.authorId,
                            userName = post.authorNickname,
                            content = post.content,
                            helpCategory = post.helpCategory,
                            urgencyLevel = post.urgencyLevel,
                            latitude = post.latitude,
                            longitude = post.longitude,
                            locationName = post.locationName
                        ))
                    }
                }
                helpRequestSyncDao.markSynced(sync.syncId, "synced", System.currentTimeMillis())
            } catch (_: Exception) {
                helpRequestSyncDao.incrementRetry(sync.syncId)
            }
        }
    }

    suspend fun getOpenHelpRequestCount(): Int {
        return postCacheDao.getOpenHelpRequestCount()
    }

    suspend fun getUserHelpRequestCount(userId: String): Int {
        return postCacheDao.getUserHelpRequestCount(userId)
    }

    suspend fun getTopVolunteers(limit: Int = 10): List<VolunteerProfileEntity> {
        return volunteerProfileDao.getTopVolunteers(limit)
    }

    fun observePosts(pageSize: Int = 50): Flow<List<CommunityPost>> = flow {
        emit(postCacheDao.getCachedPosts(pageSize).map { it.toCommunityPost() })
    }

    suspend fun cleanupOldPosts() {
        postCacheDao.deleteOldPosts(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
    }

    private fun PostCacheEntity.toCommunityPost(): CommunityPost {
        val comments = mutableListOf<PostComment>()
        try {
            val commentsStr = this.comments ?: "[]"
            if (commentsStr.isNotEmpty()) {
                val arr = JSONArray(commentsStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    comments.add(PostComment(
                        id = obj.optString("id", ""),
                        authorName = obj.optString("authorName", ""),
                        content = obj.optString("content", ""),
                        timestamp = obj.optLong("timestamp", 0),
                        isFromVolunteer = obj.optBoolean("isFromVolunteer", false)
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PostRepository", "parse comments error: ${e.message}", e)
        }

        return CommunityPost(
            id = postId,
            userId = authorId,
            userName = authorNickname,
            content = content,
            postType = runCatching { PostType.valueOf(postType) }.getOrDefault(PostType.TEXT),
            status = runCatching { PostStatus.valueOf(status) }.getOrDefault(PostStatus.OPEN),
            likes = likeCount,
            comments = commentCount,
            timestamp = createdAt,
            location = locationName,
            latitude = latitude,
            longitude = longitude,
            volunteerName = volunteerName,
            timeAgo = getTimeAgo(createdAt),
            helpCategory = runCatching { HelpCategory.valueOf(helpCategory) }.getOrDefault(HelpCategory.OTHER),
            urgencyLevel = runCatching { UrgencyLevel.valueOf(urgencyLevel) }.getOrDefault(UrgencyLevel.LOW),
            commentList = comments
        )
    }

    private fun getTimeAgo(ts: Long): String {
        val diff = System.currentTimeMillis() - ts
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "刚刚"
            diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)}分钟前"
            diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)}小时前"
            else -> "${diff / TimeUnit.DAYS.toMillis(1)}天前"
        }
    }
}