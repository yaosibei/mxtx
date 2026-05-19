package com.mindeye.app.core.network

import com.mindeye.app.core.model.PostItem
import com.mindeye.app.core.model.EmergencyContact
import retrofit2.http.*

/**
 * 社区服务接口
 * 用于盲人社区互动功能 - 增强版，包含志愿者接单、评价等完整功能
 */
interface CommunityApiService {

    /**
     * 获取社区帖子列表
     */
    @GET("v1/community/posts")
    suspend fun getPosts(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("type") type: String? = null
    ): PostsResponse

    /**
     * 发布新帖子
     */
    @POST("v1/community/posts")
    suspend fun createPost(@Body request: CreatePostRequest): PostResponse

    /**
     * 获取帖子详情
     */
    @GET("v1/community/posts/{postId}")
    suspend fun getPostDetail(@Path("postId") postId: String): PostDetailResponse

    /**
     * 发布评论
     */
    @POST("v1/community/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body request: CreateCommentRequest
    ): CommentResponse

    /**
     * 点赞/取消点赞
     */
    @POST("v1/community/posts/{postId}/like")
    suspend fun toggleLike(@Path("postId") postId: String): LikeResponse

    /**
     * 一键求助
     */
    @POST("v1/emergency/request")
    suspend fun requestEmergencyHelp(@Body request: EmergencyRequest): EmergencyResponse

    /**
     * 获取紧急联系人
     */
    @GET("v1/emergency/contacts")
    suspend fun getEmergencyContacts(): EmergencyContactsResponse

    /**
     * 发布求助请求
     */
    @POST("v1/community/help-requests")
    suspend fun createHelpRequest(@Body request: CreateHelpRequest): HelpRequestResponse

    /**
     * 获取求助列表
     */
    @GET("v1/community/help-requests")
    suspend fun getHelpRequests(
        @Query("status") status: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): HelpRequestsResponse

    /**
     * 志愿者接单
     */
    @POST("v1/community/help-requests/{postId}/accept")
    suspend fun acceptHelpRequest(
        @Path("postId") postId: String,
        @Body request: AcceptHelpRequest
    ): AcceptResponse

    /**
     * 完成求助
     */
    @POST("v1/community/help-requests/{postId}/complete")
    suspend fun completeHelpRequest(
        @Path("postId") postId: String,
        @Body request: CompleteHelpRequest
    ): CompleteResponse

    /**
     * 评价求助服务
     */
    @POST("v1/community/help-requests/{postId}/rate")
    suspend fun rateHelpRequest(
        @Path("postId") postId: String,
        @Body request: RateHelpRequest
    ): RateResponse

    /**
     * 获取志愿者信息
     */
    @GET("v1/community/volunteers/{volunteerId}")
    suspend fun getVolunteerProfile(@Path("volunteerId") volunteerId: String): VolunteerProfileResponse

    /**
     * 更新志愿者信息
     */
    @PUT("v1/community/volunteers/{volunteerId}")
    suspend fun updateVolunteerProfile(
        @Path("volunteerId") volunteerId: String,
        @Body request: UpdateVolunteerRequest
    ): VolunteerProfileResponse

    /**
     * 获取志愿者排行榜
     */
    @GET("v1/community/volunteers/ranking")
    suspend fun getVolunteerRanking(
        @Query("limit") limit: Int = 10
    ): VolunteerRankingResponse

    /**
     * 获取我的求助历史
     */
    @GET("v1/community/users/{userId}/help-history")
    suspend fun getUserHelpHistory(@Path("userId") userId: String): HelpHistoryResponse

    /**
     * 获取志愿者的接单历史
     */
    @GET("v1/community/volunteers/{volunteerId}/orders")
    suspend fun getVolunteerOrders(@Path("volunteerId") volunteerId: String): VolunteerOrdersResponse
}

/**
 * 帖子列表响应
 */
data class PostsResponse(
    val posts: List<PostItem>,
    val total: Int,
    val hasMore: Boolean
)

/**
 * 作者信息
 */
data class AuthorInfo(
    val id: String,
    val nickname: String,
    val avatar: String?
)

/**
 * 帖子详情响应
 */
data class PostDetailResponse(
    val post: PostItem,
    val comments: List<CommentItem>
)

/**
 * 评论项
 */
data class CommentItem(
    val id: String,
    val content: String,
    val author: AuthorInfo,
    val createdAt: Long,
    val likeCount: Int
)

/**
 * 创建帖子请求
 */
data class CreatePostRequest(
    val title: String,
    val content: String,
    val type: String,
    val images: List<String>?           // 图片 Base64 列表
)

/**
 * 帖子响应
 */
data class PostResponse(
    val id: String,
    val success: Boolean
)

/**
 * 创建评论请求
 */
data class CreateCommentRequest(
    val content: String
)

/**
 * 评论响应
 */
data class CommentResponse(
    val id: String,
    val success: Boolean
)

/**
 * 点赞响应
 */
data class LikeResponse(
    val isLiked: Boolean,
    val likeCount: Int
)

/**
 * 紧急求助请求
 */
data class EmergencyRequest(
    val latitude: Double,
    val longitude: Double,
    val message: String?,
    val contactIds: List<String>?       // 指定联系人，为空则通知所有紧急联系人
)

/**
 * 紧急求助响应
 */
data class EmergencyResponse(
    val requestId: String,
    val status: String,                 // pending/sent/failed
    val notifiedContacts: List<String>  // 已通知的联系人
)

/**
 * 紧急联系人响应
 */
data class EmergencyContactsResponse(
    val contacts: List<EmergencyContact>
)

/**
 * 紧急联系人
 */
data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String
)

/**
 * 创建求助请求
 */
data class CreateHelpRequest(
    val userId: String,
    val userName: String,
    val content: String,
    val helpCategory: String = "OTHER",
    val urgencyLevel: String = "LOW",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null
)

/**
 * 求助响应
 */
data class HelpRequestResponse(
    val postId: String,
    val success: Boolean,
    val message: String? = null
)

/**
 * 求助列表响应
 */
data class HelpRequestsResponse(
    val requests: List<HelpRequestItem>,
    val total: Int,
    val hasMore: Boolean
)

/**
 * 求助项
 */
data class HelpRequestItem(
    val id: String,
    val userId: String,
    val userName: String,
    val content: String,
    val helpCategory: String,
    val urgencyLevel: String,
    val status: String,
    val locationName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val volunteerName: String?,
    val createdAt: Long,
    val commentCount: Int = 0,
    val likeCount: Int = 0
)

/**
 * 接单请求
 */
data class AcceptHelpRequest(
    val volunteerId: String,
    val volunteerName: String
)

/**
 * 接单响应
 */
data class AcceptResponse(
    val orderId: String,
    val success: Boolean,
    val message: String? = null
)

/**
 * 完成请求
 */
data class CompleteHelpRequest(
    val volunteerId: String,
    val notes: String? = null
)

/**
 * 完成响应
 */
data class CompleteResponse(
    val success: Boolean,
    val message: String? = null
)

/**
 * 评价请求
 */
data class RateHelpRequest(
    val userId: String,
    val rating: Int,
    val feedback: String? = null
)

/**
 * 评价响应
 */
data class RateResponse(
    val success: Boolean,
    val message: String? = null
)

/**
 * 志愿者资料响应
 */
data class VolunteerProfileResponse(
    val volunteerId: String,
    val userId: String,
    val name: String,
    val avatar: String?,
    val serviceCount: Int,
    val completedOrders: Int,
    val rating: Double,
    val totalHours: Long,
    val joinDate: Long,
    val level: String,
    val badges: List<VolunteerBadge>
)

/**
 * 志愿者徽章
 */
data class VolunteerBadge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val earnedDate: Long?
)

/**
 * 更新志愿者请求
 */
data class UpdateVolunteerRequest(
    val name: String? = null,
    val avatar: String? = null,
    val isActive: Boolean? = null
)

/**
 * 志愿者排行榜响应
 */
data class VolunteerRankingResponse(
    val volunteers: List<RankedVolunteer>
)

/**
 * 排行榜志愿者
 */
data class RankedVolunteer(
    val volunteerId: String,
    val name: String,
    val avatar: String?,
    val serviceCount: Int,
    val rating: Double,
    val rank: Int
)

/**
 * 帮助历史响应
 */
data class HelpHistoryResponse(
    val history: List<HelpHistoryItem>
)

/**
 * 帮助历史项
 */
data class HelpHistoryItem(
    val historyId: String,
    val postId: String,
    val helperId: String,
    val helperName: String,
    val helpCategory: String,
    val content: String,
    val rating: Int,
    val feedback: String?,
    val createdAt: Long,
    val completedAt: Long?
)

/**
 * 志愿者订单响应
 */
data class VolunteerOrdersResponse(
    val orders: List<VolunteerOrderItem>
)

/**
 * 志愿者订单项
 */
data class VolunteerOrderItem(
    val orderId: String,
    val postId: String,
    val requesterId: String,
    val requesterName: String,
    val status: String,
    val content: String,
    val helpCategory: String,
    val createdAt: Long,
    val acceptedAt: Long?,
    val completedAt: Long?,
    val rating: Int,
    val feedback: String?
)
