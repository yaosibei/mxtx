package com.mindeye.app.feature.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.core.model.AiQaMessage
import com.mindeye.app.core.model.CommunityPost
import com.mindeye.app.core.model.Encouragement
import com.mindeye.app.core.model.HelpRequest
import com.mindeye.app.core.model.HelpRequestStatus
import com.mindeye.app.core.model.PostType
import com.mindeye.app.feature.community.data.AiQaRepository
import com.mindeye.app.feature.community.data.EncouragementRepository
import com.mindeye.app.feature.community.data.HelpRequestRepository
import com.mindeye.app.feature.community.data.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val posts: List<CommunityPost> = emptyList(),
    val helpRequests: List<HelpRequest> = emptyList(),
    val aiQaMessages: List<AiQaMessage> = emptyList(),
    val encouragements: List<Encouragement> = emptyList(),
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val isSendingHelpRequest: Boolean = false,
    val isAiTyping: Boolean = false,
    val errorMessage: String? = null,
    val currentTab: CommunityTab = CommunityTab.POSTS
)

enum class CommunityTab {
    POSTS,          // 社区帖子
    HELP_REQUESTS,  // 求助与接单
    VOLUNTEER,      // 志愿者中心
    AI_QA,          // AI问答
    ENCOURAGEMENTS  // 鼓励语
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val helpRequestRepository: HelpRequestRepository,
    private val aiQaRepository: AiQaRepository,
    private val encouragementRepository: EncouragementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val currentUserId = "local_user"
    private val currentUserName = "我"

    init {
        loadPosts()
        loadHelpRequests()
        loadEncouragements()
    }

    // ==================== Tab 切换 ====================

    fun switchTab(tab: CommunityTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
        when (tab) {
            CommunityTab.POSTS -> loadPosts()
            CommunityTab.HELP_REQUESTS -> loadHelpRequests()
            CommunityTab.VOLUNTEER -> {}
            CommunityTab.AI_QA -> loadAiQaHistory()
            CommunityTab.ENCOURAGEMENTS -> loadEncouragements()
        }
    }

    // ==================== 社区帖子 ====================

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
        // 添加求助示例帖子
        postRepository.createPost(
            "user4", "志愿者小张",
            "今天在社区接到一个求助，帮一位阿姨找到了去医院的路线，很开心能帮到别人！",
            PostType.TEXT
        )
    }

    // ==================== 求助与接单 ====================

    fun loadHelpRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val requests = helpRequestRepository.getPendingRequests()
                _uiState.value = _uiState.value.copy(
                    helpRequests = requests,
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

    fun createHelpRequest(content: String, location: String?) {
        android.util.Log.d("CommunityVM", "=== createHelpRequest 被调用 ===")
        android.util.Log.d("CommunityVM", "Content: $content")
        android.util.Log.d("CommunityVM", "Location: $location")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingHelpRequest = true)
            try {
                android.util.Log.d("CommunityVM", "开始调用 repository.createRequest")
                val success = helpRequestRepository.createRequest(
                    requesterId = currentUserId,
                    requesterName = currentUserName,
                    content = content,
                    location = location
                )
                android.util.Log.d("CommunityVM", "createRequest 返回: $success")
                if (success) {
                    val requests = helpRequestRepository.getPendingRequests()
                    android.util.Log.d("CommunityVM", "查询到 ${requests.size} 条求助")
                    _uiState.value = _uiState.value.copy(helpRequests = requests)
                } else {
                    android.util.Log.e("CommunityVM", "创建求助失败！")
                }
            } catch (e: Exception) {
                android.util.Log.e("CommunityVM", "创建求助异常", e)
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
            _uiState.value = _uiState.value.copy(isSendingHelpRequest = false)
        }
    }

    fun acceptHelpRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val success = helpRequestRepository.acceptRequest(
                    requestId = requestId,
                    volunteerId = currentUserId,
                    volunteerName = currentUserName
                )
                if (success) {
                    loadHelpRequests()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun completeHelpRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val success = helpRequestRepository.completeRequest(requestId)
                if (success) {
                    loadHelpRequests()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    // ==================== AI 问答 ====================

    fun loadAiQaHistory() {
        viewModelScope.launch {
            try {
                val messages = aiQaRepository.getQaHistory(currentUserId)
                if (messages.isEmpty()) {
                    seedSampleAiMessages()
                }
                val updatedMessages = aiQaRepository.getQaHistory(currentUserId)
                _uiState.value = _uiState.value.copy(aiQaMessages = updatedMessages)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun sendAiQuestion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            val userMessage = AiQaMessage(
                content = question,
                isFromUser = true
            )
            val currentMessages = _uiState.value.aiQaMessages.toMutableList()
            currentMessages.add(userMessage)
            _uiState.value = _uiState.value.copy(aiQaMessages = currentMessages, isAiTyping = true)

            delay(600)

            val aiAnswer = aiQaRepository.generateAiAnswer(question)
            val aiMessage = AiQaMessage(
                content = aiAnswer,
                isFromUser = false
            )
            currentMessages.add(aiMessage)

            aiQaRepository.saveQa(currentUserId, question, aiAnswer)

            _uiState.value = _uiState.value.copy(
                aiQaMessages = currentMessages,
                isAiTyping = false
            )
        }
    }

    private fun seedSampleAiMessages() {
        _uiState.value = _uiState.value.copy(
            aiQaMessages = listOf(
                AiQaMessage(
                    content = "你好！我是社区AI助手，可以帮你解答社区求助、志愿者接单、出行导航等相关问题。",
                    isFromUser = false
                )
            )
        )
    }

    // ==================== 鼓励语 ====================

    fun loadEncouragements() {
        viewModelScope.launch {
            try {
                val encouragements = encouragementRepository.getUserEncouragements(currentUserId)
                if (encouragements.isEmpty()) {
                    generateEncouragement()
                }
                val updatedEncouragements = encouragementRepository.getUserEncouragements(currentUserId)
                _uiState.value = _uiState.value.copy(encouragements = updatedEncouragements)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun generateEncouragement() {
        viewModelScope.launch {
            try {
                val encouragement = encouragementRepository.generateEncouragementFromTravel(currentUserId)
                encouragement?.let {
                    val currentList = _uiState.value.encouragements.toMutableList()
                    currentList.add(0, it)
                    _uiState.value = _uiState.value.copy(encouragements = currentList)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun markEncouragementAsRead(id: String) {
        viewModelScope.launch {
            try {
                encouragementRepository.markAsRead(id)
                loadEncouragements()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    // ==================== 通用 ====================

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
