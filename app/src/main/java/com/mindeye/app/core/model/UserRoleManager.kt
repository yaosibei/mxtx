package com.mindeye.app.core.model

import com.mindeye.app.core.database.dao.SettingsDao
import com.mindeye.app.core.database.dao.UserDao
import com.mindeye.app.core.database.entity.SettingsEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@Singleton
class UserRoleManager @Inject constructor(
    private val settingsDao: SettingsDao,
    private val userDao: UserDao
) {
    companion object {
        const val KEY_ROLE = "user_role"
        const val KEY_SERVICE_COUNT = "volunteer_service_count"
        const val KEY_BADGES = "user_badges"
        const val ROLE_USER = "user"
        const val ROLE_VOLUNTEER = "volunteer"
    }

    suspend fun setUserRole(role: String) {
        settingsDao.insertSetting(
            SettingsEntity(
                key = KEY_ROLE,
                value = role,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getUserRole(): String {
        return settingsDao.getSettingValue(KEY_ROLE) ?: ROLE_USER
    }

    suspend fun isVolunteer(): Boolean {
        return getUserRole() == ROLE_VOLUNTEER
    }

    suspend fun getServiceCount(): Int {
        return settingsDao.getSettingValue(KEY_SERVICE_COUNT)?.toIntOrNull() ?: 0
    }

    suspend fun incrementServiceCount(): Int {
        val count = getServiceCount() + 1
        settingsDao.insertSetting(
            SettingsEntity(
                key = KEY_SERVICE_COUNT,
                value = count.toString(),
                updatedAt = System.currentTimeMillis()
            )
        )
        return count
    }

    suspend fun getHelpHistory(): List<HelpHistoryRecord> {
        return listOf(
            HelpHistoryRecord("post_1", "老李", "导航到地铁站", "NAVIGATION", "completed", System.currentTimeMillis() - 86400000, System.currentTimeMillis() - 86300000),
            HelpHistoryRecord("post_2", "王阿姨", "帮忙拎东西", "CARRY_ITEMS", "completed", System.currentTimeMillis() - 172800000, System.currentTimeMillis() - 172700000)
        )
    }

    suspend fun getBadges(): List<Badge> {
        val serviceCount = getServiceCount()
        val badges = mutableListOf<Badge>()
        if (serviceCount >= 1) {
            badges.add(Badge("first_help", "初次帮助", "完成第一次志愿服务", "star", System.currentTimeMillis()))
        }
        if (serviceCount >= 5) {
            badges.add(Badge("helper", "热心帮手", "完成5次志愿服务", "favorite", System.currentTimeMillis()))
        }
        if (serviceCount >= 20) {
            badges.add(Badge("expert", "志愿达人", "完成20次志愿服务", "emoji_events", System.currentTimeMillis()))
        }
        return badges
    }

    fun observeUserRole(): Flow<String> {
        return settingsDao.observeSettingValue(KEY_ROLE).map { it ?: ROLE_USER }
    }

    fun getUserProfile(): Flow<UserProfile?> = flow {
        val role = getUserRole()
        val serviceCount = getServiceCount()
        val badges = getBadges()
        val stats = if (role == ROLE_VOLUNTEER) {
            VolunteerStats(
                totalServices = serviceCount,
                completedOrders = serviceCount,
                pendingOrders = 0,
                totalHours = serviceCount * 2L,
                rating = 4.8
            )
        } else null
        emit(
            UserProfile(
                userId = "local_user",
                nickname = "我",
                role = role,
                travelStats = null,
                volunteerStats = stats
            )
        )
    }
}
