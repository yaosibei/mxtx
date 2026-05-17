package com.mindeye.app.feature.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.core.model.CommunityPost
import com.mindeye.app.core.model.PostType
import com.mindeye.app.feature.community.data.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val posts: List<CommunityPost> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPosting: Boolean = false
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val cachedPosts = postRepository.getCachedPosts()
                if (cachedPosts.isEmpty()) {
                    seedSamplePosts()
                }
                val posts = postRepository.getCachedPosts()
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun createPost(userId: String, userName: String, content: String, postType: PostType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPosting = true)
            try {
                val success = postRepository.createPost(userId, userName, content, postType)
                if (success) {
                    loadPosts()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
            _uiState.value = _uiState.value.copy(isPosting = false)
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                postRepository.likePost(postId)
                loadPosts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun addComment(postId: String, userId: String, userName: String, content: String) {
        viewModelScope.launch {
            try {
                postRepository.addComment(postId, userId, userName, content)
                loadPosts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    private suspend fun seedSamplePosts() {
        postRepository.createPost(
            "user1", "小明",
            "今天第一次用明心同行出门，场景分析真的很准确！震动提醒帮我避开了很多障碍。",
            PostType.TEXT
        )
        postRepository.createPost(
            "user2", "小红",
            "请问大家平时是怎么使用语音输入功能的？有没有什么小技巧可以分享？",
            PostType.TEXT
        )
        postRepository.createPost(
            "user3", "老李",
            "感谢开发团队做出这么贴心的应用，特别是心理支持功能，给了我很大帮助。",
            PostType.TEXT
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
