package com.mindeye.app.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindeye.app.core.model.CleanupOldRecordsUseCase
import com.mindeye.app.feature.senseflow.data.SceneRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val cleanupOldRecordsUseCase: CleanupOldRecordsUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            cleanupOldRecordsUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

@HiltWorker
class SyncRecordsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sceneRepository: SceneRepository,
    private val cleanupOldRecordsUseCase: CleanupOldRecordsUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            cleanupOldRecordsUseCase()
            val records = sceneRepository.getRecentRecords(100)
            android.util.Log.d("SyncRecordsWorker", "同步了 ${records.size} 条场景记录")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
