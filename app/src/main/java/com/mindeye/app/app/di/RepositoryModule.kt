package com.mindeye.app.di

import com.mindeye.app.core.database.dao.*
import com.mindeye.app.feature.senseflow.data.SceneRepository
import com.mindeye.app.feature.psychology.data.ChatHistoryRepository
import com.mindeye.app.feature.psychology.data.TravelRecordRepository
import com.mindeye.app.feature.community.data.PostRepository
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
    fun providePostRepository(
        postCacheDao: PostCacheDao,
        volunteerOrderDao: VolunteerOrderDao,
        volunteerProfileDao: VolunteerProfileDao,
        helpHistoryDao: HelpHistoryDao,
        userBadgeDao: UserBadgeDao,
        helpRequestSyncDao: HelpRequestSyncDao
    ): PostRepository {
        return PostRepository(
            postCacheDao,
            volunteerOrderDao,
            volunteerProfileDao,
            helpHistoryDao,
            userBadgeDao,
            helpRequestSyncDao,
            null
        )
    }

    @Provides
    @Singleton
    fun provideEmergencyContactRepository(emergencyContactDao: EmergencyContactDao): EmergencyContactRepository {
        return EmergencyContactRepository(emergencyContactDao)
    }

    @Provides
    @Singleton
    fun provideTravelRecordRepository(travelRecordDao: TravelRecordDao): TravelRecordRepository {
        return TravelRecordRepository(travelRecordDao)
    }
}
