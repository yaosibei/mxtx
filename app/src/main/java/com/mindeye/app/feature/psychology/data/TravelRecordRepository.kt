package com.mindeye.app.feature.psychology.data

import com.mindeye.app.core.database.dao.TravelRecordDao
import com.mindeye.app.core.database.entity.TravelRecordEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TravelRecordRepository @Inject constructor(
    private val travelRecordDao: TravelRecordDao
) {

    suspend fun getUserRecords(userId: String, limit: Int = 50): List<TravelRecordEntity> {
        return travelRecordDao.getUserRecords(userId, limit)
    }

    suspend fun insertRecord(record: TravelRecordEntity) {
        travelRecordDao.insertRecord(record)
    }

    suspend fun getUserRecordCount(userId: String): Int {
        return travelRecordDao.getUserRecordCount(userId)
    }

    suspend fun getUserTotalDistance(userId: String): Double {
        return travelRecordDao.getUserTotalDistance(userId)
    }

    suspend fun getUserTotalDuration(userId: String): Long {
        return travelRecordDao.getUserTotalDuration(userId)
    }

    suspend fun getLatestCompletedRecord(userId: String): TravelRecordEntity? {
        return travelRecordDao.getLatestCompletedRecord(userId)
    }
}
