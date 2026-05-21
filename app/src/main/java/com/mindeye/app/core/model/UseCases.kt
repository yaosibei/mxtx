package com.mindeye.app.core.model

import com.mindeye.app.feature.senseflow.data.SceneRepository
import com.mindeye.app.feature.psychology.data.ChatHistoryRepository
import com.mindeye.app.feature.sos.data.EmergencyContactRepository
import com.mindeye.app.feature.community.data.PostRepository
import com.mindeye.app.core.database.entity.SceneRecordEntity
import com.mindeye.app.core.database.entity.ChatHistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetRecentScenesUseCase @Inject constructor(
    private val repository: SceneRepository
) {
    suspend operator fun invoke(limit: Int = 10) = repository.getRecentRecords(limit)
}

@Singleton
class SaveSceneRecordUseCase @Inject constructor(
    private val repository: SceneRepository
) {
    suspend operator fun invoke(record: SceneRecordEntity) =
        repository.saveRecord(record)
}

@Singleton
class GetRecentChatsUseCase @Inject constructor(
    private val repository: ChatHistoryRepository
) {
    suspend operator fun invoke(limit: Int = 50) = repository.getRecentChats(limit)
}

@Singleton
class SaveChatUseCase @Inject constructor(
    private val repository: ChatHistoryRepository
) {
    suspend operator fun invoke(chat: ChatHistoryEntity) =
        repository.saveChat(chat)
}

@Singleton
class GetEmergencyContactsUseCase @Inject constructor(
    private val repository: EmergencyContactRepository
) {
    suspend operator fun invoke() = repository.getAllContacts()
}

@Singleton
class GetDefaultContactsUseCase @Inject constructor(
    private val repository: EmergencyContactRepository
) {
    suspend operator fun invoke() = repository.getDefaultContacts()
}

@Singleton
class GetCachedPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(page: Int = 0, pageSize: Int = 20) =
        repository.getCachedPosts(pageSize)
}

@Singleton
class CleanupOldRecordsUseCase @Inject constructor(
    private val sceneRepository: SceneRepository,
    private val chatRepository: ChatHistoryRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke() {
        sceneRepository.cleanupOldRecords()
        chatRepository.cleanupOldChats()
        postRepository.cleanupOldPosts()
    }
}
