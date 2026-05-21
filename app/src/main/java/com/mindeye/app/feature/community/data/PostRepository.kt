package com.mindeye.app.feature.community.data

import com.mindeye.app.core.model.CommunityPost
import com.mindeye.app.core.model.Comment
import com.mindeye.app.core.model.PostType
import com.mindeye.app.core.database.dao.CommentDao
import com.mindeye.app.core.database.dao.PostCacheDao
import com.mindeye.app.core.database.entity.PostCacheEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PostRepository(
    private val postCacheDao: PostCacheDao,
    private val commentDao: CommentDao
) {

    suspend fun getCachedPosts(limit: Int = 50): List<CommunityPost> {
        val cachedPosts = postCacheDao.getCachedPosts(limit)
        return cachedPosts.map { entity ->
            val comments = commentDao.getCommentsByPostId(entity.postId)
            CommunityPost(
                id = entity.postId,
                userId = entity.authorId,
                userName = entity.authorNickname,
                content = entity.content,
                postType = PostType.TEXT,
                likes = entity.likeCount,
                comments = entity.commentCount,
                timestamp = entity.createdAt,
                commentList = comments.map { it.toDomainModel() }
            )
        }
    }

    suspend fun getPostDetail(postId: String): CommunityPost? {
        val cachedPost = postCacheDao.getPostById(postId)
        return cachedPost?.let { entity ->
            val comments = commentDao.getCommentsByPostId(entity.postId)
            CommunityPost(
                id = entity.postId,
                userId = entity.authorId,
                userName = entity.authorNickname,
                content = entity.content,
                postType = PostType.TEXT,
                likes = entity.likeCount,
                comments = entity.commentCount,
                timestamp = entity.createdAt,
                commentList = comments.map { it.toDomainModel() }
            )
        }
    }

    suspend fun createPost(userId: String, userName: String, content: String, postType: PostType): Boolean {
        return try {
            val entity = PostCacheEntity(
                postId = System.currentTimeMillis().toString(),
                authorId = userId,
                authorNickname = userName,
                content = content,
                likeCount = 0,
                commentCount = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            postCacheDao.insertPost(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun likePost(postId: String): Boolean {
        return try {
            postCacheDao.incrementLikes(postId)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addComment(postId: String, userId: String, userName: String, content: String): Boolean {
        return try {
            val commentEntity = com.mindeye.app.core.database.entity.CommentEntity(
                commentId = System.currentTimeMillis().toString(),
                postId = postId,
                userId = userId,
                userName = userName,
                content = content
            )
            commentDao.insertComment(commentEntity)
            postCacheDao.incrementComments(postId)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cachePosts(posts: List<PostCacheEntity>) {
        posts.forEach { postCacheDao.insertPost(it) }
    }

    fun observePosts(pageSize: Int = 50): Flow<List<PostCacheEntity>> {
        return flow {
            val posts = postCacheDao.getCachedPosts(pageSize)
            emit(posts)
        }
    }

    suspend fun cleanupOldPosts() {
        val thresholdTime = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        postCacheDao.deleteOldPosts(thresholdTime)
        commentDao.deleteOldComments(thresholdTime)
    }

    private fun com.mindeye.app.core.database.entity.CommentEntity.toDomainModel(): Comment {
        return Comment(
            id = commentId,
            postId = postId,
            userId = userId,
            userName = userName,
            content = content,
            timestamp = createdAt
        )
    }
}
