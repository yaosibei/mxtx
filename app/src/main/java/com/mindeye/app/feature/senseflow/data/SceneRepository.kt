package com.mindeye.app.feature.senseflow.data

import com.mindeye.app.core.database.dao.SceneRecordDao
import com.mindeye.app.core.database.entity.SceneRecordEntity
import kotlinx.coroutines.flow.Flow

class SceneRepository(
    private val sceneRecordDao: SceneRecordDao
) {

    suspend fun saveRecord(record: SceneRecordEntity) {
        sceneRecordDao.insertRecord(record)
    }

    suspend fun getRecentRecords(limit: Int = 50): List<SceneRecordEntity> {
        return sceneRecordDao.getRecentRecords(limit)
    }

    suspend fun getRecordsByTimeRange(startTime: Long, endTime: Long): List<SceneRecordEntity> {
        return sceneRecordDao.getRecordsByTimeRange(startTime, endTime)
    }

    suspend fun getRecordsBySceneType(sceneType: String): List<SceneRecordEntity> {
        return sceneRecordDao.getRecordsBySceneType(sceneType)
    }

    suspend fun cleanupOldRecords() {
        val thresholdTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        sceneRecordDao.deleteOldRecords(thresholdTime)
    }

    fun getRecordCount(): Flow<Int> {
        return sceneRecordDao.getRecordCount()
    }
}
