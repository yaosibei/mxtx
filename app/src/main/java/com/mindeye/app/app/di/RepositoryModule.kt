package com.mindeye.app.di

import com.mindeye.app.core.database.dao.*
import com.mindeye.app.feature.senseflow.data.SceneRepository
import com.mindeye.app.feature.psychology.data.ChatHistoryRepository
import com.mindeye.app.feature.community.data.PostRepository
import com.mindeye.app.feature.community.data.HelpRequestRepository
import com.mindeye.app.feature.community.data.AiQaRepository
import com.mindeye.app.feature.community.data.EncouragementRepository
import com.mindeye.app.feature.sos.data.EmergencyContactRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSceneRepository(sceneRecordDao: SceneRecordDao): SceneRepository {
        return SceneRepository(sceneRecordDao)
    }

    @Provides
    @Singleton
    fun provideChatHistoryRepository(chatHistoryDao: ChatHistoryDao): ChatHistoryRepository {
        return ChatHistoryRepository(chatHistoryDao)
    }

    @Provides
    @Singleton
    fun providePostRepository(postCacheDao: PostCacheDao, commentDao: CommentDao): PostRepository {
        return PostRepository(postCacheDao, commentDao)
    }

    @Provides
    @Singleton
    fun provideEmergencyContactRepository(emergencyContactDao: EmergencyContactDao): EmergencyContactRepository {
        return EmergencyContactRepository(emergencyContactDao)
    }

    @Provides
    @Singleton
    fun provideHelpRequestRepository(helpRequestDao: HelpRequestDao): HelpRequestRepository {
        return HelpRequestRepository(helpRequestDao)
    }

    @Provides
    @Singleton
    fun provideAiQaRepository(aiQaDao: AiQaDao): AiQaRepository {
        return AiQaRepository(aiQaDao)
    }

    @Provides
    @Singleton
    fun provideEncouragementRepository(
        encouragementDao: EncouragementDao,
        sceneRecordDao: SceneRecordDao
    ): EncouragementRepository {
        return EncouragementRepository(encouragementDao, sceneRecordDao)
    }
}
