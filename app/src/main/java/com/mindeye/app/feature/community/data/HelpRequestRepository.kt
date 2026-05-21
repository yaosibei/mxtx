package com.mindeye.app.feature.community.data

import android.util.Log
import com.mindeye.app.core.database.dao.HelpRequestDao
import com.mindeye.app.core.database.entity.HelpRequestEntity
import com.mindeye.app.core.model.HelpRequest
import com.mindeye.app.core.model.HelpRequestStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "HelpRequestRepo"

class HelpRequestRepository(
    private val helpRequestDao: HelpRequestDao
) {

    fun getPendingRequestsFlow(limit: Int = 50): Flow<List<HelpRequest>> {
        return helpRequestDao.getPendingRequestsFlow(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getVolunteerRequestsFlow(volunteerId: String): Flow<List<HelpRequest>> {
        return helpRequestDao.getVolunteerRequestsFlow(volunteerId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getPendingRequests(limit: Int = 50): List<HelpRequest> {
        val entities = helpRequestDao.getPendingRequests(limit)
        Log.d(TAG, "=== 查询待接单求助请求 ===")
        Log.d(TAG, "查询到 ${entities.size} 条记录")
        entities.forEach { entity ->
            Log.d(TAG, "Request: ${entity.requestId}, Status: ${entity.status}, Content: ${entity.content.take(20)}...")
        }
        return entities.map { it.toDomainModel() }
    }

    suspend fun getUserRequests(userId: String): List<HelpRequest> {
        return helpRequestDao.getUserRequests(userId).map { it.toDomainModel() }
    }

    suspend fun getVolunteerRequests(volunteerId: String): List<HelpRequest> {
        return helpRequestDao.getVolunteerRequests(volunteerId).map { it.toDomainModel() }
    }

    suspend fun getRequestById(requestId: String): HelpRequest? {
        return helpRequestDao.getRequestById(requestId)?.toDomainModel()
    }

    suspend fun createRequest(
        requesterId: String,
        requesterName: String,
        content: String,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Boolean {
        return try {
            val entity = HelpRequestEntity(
                requestId = System.currentTimeMillis().toString(),
                requesterId = requesterId,
                requesterName = requesterName,
                content = content,
                location = location,
                latitude = latitude,
                longitude = longitude,
                status = "PENDING"
            )
            Log.d(TAG, "=== 创建求助请求 ===")
            Log.d(TAG, "Request ID: ${entity.requestId}")
            Log.d(TAG, "Content: ${entity.content}")
            Log.d(TAG, "Status: ${entity.status}")
            helpRequestDao.insertRequest(entity)
            Log.d(TAG, "求助请求已插入数据库")
            true
        } catch (e: Exception) {
            Log.e(TAG, "创建求助请求失败", e)
            false
        }
    }

    suspend fun acceptRequest(
        requestId: String,
        volunteerId: String,
        volunteerName: String
    ): Boolean {
        return try {
            helpRequestDao.acceptRequest(
                requestId = requestId,
                status = "ACCEPTED",
                volunteerId = volunteerId,
                volunteerName = volunteerName,
                updatedAt = System.currentTimeMillis()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun completeRequest(requestId: String): Boolean {
        return try {
            helpRequestDao.updateStatus(
                requestId = requestId,
                status = "COMPLETED",
                updatedAt = System.currentTimeMillis()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cancelRequest(requestId: String): Boolean {
        return try {
            helpRequestDao.updateStatus(
                requestId = requestId,
                status = "CANCELLED",
                updatedAt = System.currentTimeMillis()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cleanupOldRequests() {
        val thresholdTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        helpRequestDao.deleteOldRequests(thresholdTime)
    }

    private fun HelpRequestEntity.toDomainModel(): HelpRequest {
        return HelpRequest(
            id = requestId,
            requesterId = requesterId,
            requesterName = requesterName,
            content = content,
            location = location,
            latitude = latitude,
            longitude = longitude,
            status = HelpRequestStatus.valueOf(status),
            volunteerId = volunteerId,
            volunteerName = volunteerName,
            timestamp = createdAt
        )
    }
}
