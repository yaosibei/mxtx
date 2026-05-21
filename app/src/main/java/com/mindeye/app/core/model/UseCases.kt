package com.mindeye.app.core.model

import com.mindeye.app.feature.senseflow.data.SceneRepository
import com.mindeye.app.feature.psychology.data.ChatHistoryRepository
import com.mindeye.app.feature.sos.data.EmergencyContactRepository
import com.mindeye.app.feature.community.data.PostRepository
import com.mindeye.app.feature.community.data.HelpRequestRepository
import com.mindeye.app.feature.community.data.AiQaRepository
import com.mindeye.app.feature.community.data.EncouragementRepository
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
    private val postRepository: PostRepository,
    private val helpRequestRepository: HelpRequestRepository,
    private val aiQaRepository: AiQaRepository,
    private val encouragementRepository: EncouragementRepository
) {
    suspend operator fun invoke() {
        sceneRepository.cleanupOldRecords()
        chatRepository.cleanupOldChats()
        postRepository.cleanupOldPosts()
        helpRequestRepository.cleanupOldRequests()
        aiQaRepository.cleanupOldQa()
        encouragementRepository.cleanupOldEncouragements()
    }
}

// ==================== 社区求助与接单 UseCase ====================

@Singleton
class GetPendingHelpRequestsUseCase @Inject constructor(
    private val repository: HelpRequestRepository
) {
    suspend operator fun invoke(limit: Int = 50) = repository.getPendingRequests(limit)
}

@Singleton
class CreateHelpRequestUseCase @Inject constructor(
    private val repository: HelpRequestRepository
) {
    suspend operator fun invoke(
        requesterId: String,
        requesterName: String,
        content: String,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) = repository.createRequest(requesterId, requesterName, content, location, latitude, longitude)
}

@Singleton
class AcceptHelpRequestUseCase @Inject constructor(
    private val repository: HelpRequestRepository
) {
    suspend operator fun invoke(requestId: String, volunteerId: String, volunteerName: String) =
        repository.acceptRequest(requestId, volunteerId, volunteerName)
}

@Singleton
class CompleteHelpRequestUseCase @Inject constructor(
    private val repository: HelpRequestRepository
) {
    suspend operator fun invoke(requestId: String) = repository.completeRequest(requestId)
}

// ==================== AI问答 UseCase ====================

@Singleton
class GetAiQaHistoryUseCase @Inject constructor(
    private val repository: AiQaRepository
) {
    suspend operator fun invoke(userId: String, limit: Int = 50) = repository.getQaHistory(userId, limit)
}

@Singleton
class SaveAiQaUseCase @Inject constructor(
    private val repository: AiQaRepository
) {
    suspend operator fun invoke(userId: String, question: String, answer: String) =
        repository.saveQa(userId, question, answer)
}

@Singleton
class GenerateAiAnswerUseCase @Inject constructor(
    private val repository: AiQaRepository
) {
    operator fun invoke(question: String) = repository.generateAiAnswer(question)
}

// ==================== 鼓励语 UseCase ====================

@Singleton
class GetEncouragementsUseCase @Inject constructor(
    private val repository: EncouragementRepository
) {
    suspend operator fun invoke(userId: String, limit: Int = 20) = repository.getUserEncouragements(userId, limit)
}

@Singleton
class GenerateEncouragementFromTravelUseCase @Inject constructor(
    private val repository: EncouragementRepository
) {
    suspend operator fun invoke(userId: String) = repository.generateEncouragementFromTravel(userId)
}

@Singleton
class MarkEncouragementAsReadUseCase @Inject constructor(
    private val repository: EncouragementRepository
) {
    suspend operator fun invoke(id: String) = repository.markAsRead(id)
}
