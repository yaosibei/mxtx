package com.mindeye.app.core.network

import com.mindeye.app.core.model.PostItem
import com.mindeye.app.core.model.EmergencyContact
import retrofit2.http.*

/**
 * 社区服务接口
 * 用于盲人社区互动功能
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
