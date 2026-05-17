package com.mindeye.app.di

import android.content.Context
import androidx.room.Room
import com.mindeye.app.core.database.AppDatabase
import com.mindeye.app.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mindeye_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideSceneRecordDao(database: AppDatabase): SceneRecordDao {
        return database.sceneRecordDao()
    }

    @Provides
    @Singleton
    fun provideChatHistoryDao(database: AppDatabase): ChatHistoryDao {
        return database.chatHistoryDao()
    }

    @Provides
    @Singleton
    fun providePostCacheDao(database: AppDatabase): PostCacheDao {
        return database.postCacheDao()
    }

    @Provides
    @Singleton
    fun provideEmergencyContactDao(database: AppDatabase): EmergencyContactDao {
        return database.emergencyContactDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDao(database: AppDatabase): SettingsDao {
        return database.settingsDao()
    }
}
