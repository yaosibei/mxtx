package com.mindeye.app.feature.psychology.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.core.model.ChatMessage
import com.mindeye.app.core.model.GetRecentChatsUseCase
import com.mindeye.app.core.model.SaveChatUseCase
import com.mindeye.app.core.database.entity.ChatHistoryEntity
import com.mindeye.app.di.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupportUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val currentMood: String = "normal",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val getRecentChatsUseCase: GetRecentChatsUseCase,
    private val saveChatUseCase: SaveChatUseCase,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    init {
        loadChatHistory()
    }

    fun setCurrentMood(mood: String) {
        _uiState.value = _uiState.value.copy(currentMood = mood)
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val userMessage = ChatMessage(
                content = message,
                isFromUser = true,
                mood = _uiState.value.currentMood
            )
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(userMessage)
            _uiState.value = _uiState.value.copy(messages = currentMessages, isTyping = true)

            val userEntity = ChatHistoryEntity(
                userId = "local_user",
                message = message,
                reply = "",
                mood = _uiState.value.currentMood,
                timestamp = System.currentTimeMillis(),
                isFromUser = true
            )
            saveChatUseCase(userEntity)

            delay(800)

            val aiReply = generateAiReply(message, _uiState.value.currentMood)
            val aiMessage = ChatMessage(content = aiReply, isFromUser = false)
            currentMessages.add(aiMessage)

            val aiEntity = ChatHistoryEntity(
                userId = "local_user",
                message = "",
                reply = aiReply,
                mood = _uiState.value.currentMood,
                timestamp = System.currentTimeMillis(),
                isFromUser = false
            )
            saveChatUseCase(aiEntity)

            _uiState.value = _uiState.value.copy(messages = currentMessages, isTyping = false)
            ttsManager.speak(aiReply)
        }
    }

    private fun generateAiReply(message: String, mood: String): String {
        val lowerMsg = message.lowercase()
        // 情绪维度
        val moodResponse = when (mood) {
            "happy" -> listOf(
                "听到你心情不错，我也很开心！继续保持这份好心情哦。",
                "快乐是最好的能量，你笑起来的样子一定很美。",
                "今天有什么好事？愿意和我多说说吗？"
            )
            "sad" -> listOf(
                "情绪低落的时候，有人陪伴就不那么难了。我在这里。",
                "没关系的，难过也是生活的一部分，慢慢来，会好起来的。",
                "想哭就哭出来吧，我会一直在这里听你说。"
            )
            "anxious" -> listOf(
                "焦虑很正常，深呼吸三次，我们一步一步来。",
                "你的感受是真实的，不用急着解决所有问题。",
                "紧张的时候，试着摸摸身边柔软的东西，会有帮助。"
            )
            "angry" -> listOf(
                "生气也是合理的情绪，但别让气伤到自己。",
                "先喝杯温水，慢慢告诉我发生了什么。",
                "愤怒说明你在乎，我们可以一起想办法。"
            )
            else -> listOf(
                "我在认真听你说，继续讲吧。",
                "你的感受很重要，我在这里陪着你。",
                "无论什么事情，都不是你一个人在面对。"
            )
        }

        // 关键词匹配扩展回复
        val keywordResponse = when {
            lowerMsg.contains("累") || lowerMsg.contains("辛苦") -> listOf(
                "出门确实不容易，你已经在努力了，这本身就很棒。",
                "辛苦了，休息一下再继续，没关系的。"
            )
            lowerMsg.contains("害怕") || lowerMsg.contains("担心") -> listOf(
                "害怕是自然的反应，明心同行会一直陪着你，不用怕。",
                "有我在呢，遇到不确定的情况可以随时问我。"
            )
            lowerMsg.contains("孤独") || lowerMsg.contains("一个人") -> listOf(
                "你不是一个人，有很多人关心你，包括我。",
                "社区里有很多和你一样的人在互相帮助，我们都在。"
            )
            lowerMsg.contains("感谢") || lowerMsg.contains("谢谢") -> listOf(
                "不用客气，能帮助到你我很开心。",
                "你的感谢就是最好的鼓励。"
            )
            lowerMsg.contains("出行") || lowerMsg.contains("路") || lowerMsg.contains("走") -> listOf(
                "出行安全最重要，明心之眼会帮你检查环境，放心走吧。",
                "每次出行都是一次勇敢的尝试，你做得很好。"
            )
            else -> emptyList()
        }

        val allResponses = moodResponse + keywordResponse
        return if (allResponses.isNotEmpty()) {
            allResponses.random()
        } else {
            moodResponse.random()
        }
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                val chats = getRecentChatsUseCase(50)
                val messages = chats.map { entity ->
                    if (entity.isFromUser) {
                        ChatMessage(
                            id = entity.id,
                            content = entity.message,
                            isFromUser = true,
                            timestamp = entity.timestamp,
                            mood = entity.mood
                        )
                    } else {
                        ChatMessage(
                            id = entity.id,
                            content = entity.reply,
                            isFromUser = false,
                            timestamp = entity.timestamp
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(messages = messages.reversed())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
