package com.mindeye.app.feature.psychology.data

import com.mindeye.app.core.database.dao.ChatHistoryDao
import com.mindeye.app.core.database.entity.ChatHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ChatHistoryRepository(
    private val chatHistoryDao: ChatHistoryDao
) {

    suspend fun saveChat(chat: ChatHistoryEntity) {
        chatHistoryDao.insertChat(chat)
    }

    suspend fun getRecentChats(limit: Int = 100): List<ChatHistoryEntity> {
        return chatHistoryDao.getRecentChats(limit)
    }

    suspend fun getUserChatHistory(userId: String): List<ChatHistoryEntity> {
        return chatHistoryDao.getUserChatHistory(userId)
    }

    suspend fun cleanupOldChats() {
        val thresholdTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        chatHistoryDao.deleteOldChats(thresholdTime)
    }

    suspend fun clearAllChats() {
        chatHistoryDao.deleteAllChats()
    }

    fun observeRecentChats(limit: Int = 50): Flow<List<ChatHistoryEntity>> {
        return flow {
            val chats = chatHistoryDao.getRecentChats(limit)
            emit(chats)
        }
    }
}
