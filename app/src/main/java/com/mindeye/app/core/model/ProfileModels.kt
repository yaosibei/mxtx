package com.mindeye.app.core.model

enum class HelpCategory(val label: String, val icon: String) {
    NAVIGATION("导航引导", "directions_walk"),
    CARRY_ITEMS("搬运协助", "shopping_bag"),
    EMERGENCY("紧急求助", "emergency"),
    COMPANION("陪伴出行", "groups"),
    OTHER("其他帮助", "help")
}

enum class UrgencyLevel(val label: String, val colorHex: String) {
    LOW("不急", "4CAF50"),
    MEDIUM("尽快", "FF9800"),
    HIGH("紧急", "F44336")
}

data class VolunteerProfile(
    val volunteerId: String,
    val name: String,
    val avatar: String?,
    val serviceCount: Int,
    val completedOrders: Int,
    val rating: Double,
    val joinDate: Long,
    val badges: List<Badge>
)

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val earnedDate: Long?
)

data class UserProfile(
    val userId: String,
    val nickname: String,
    val role: String,
    val travelStats: TravelStats?,
    val volunteerStats: VolunteerStats?
)

data class TravelStats(
    val totalTrips: Int,
    val totalDistance: Double,
    val totalDuration: Long,
    val favoriteScene: String?
)

data class VolunteerStats(
    val totalServices: Int,
    val completedOrders: Int,
    val pendingOrders: Int,
    val totalHours: Long,
    val rating: Double
)

data class HelpHistoryRecord(
    val postId: String,
    val requesterName: String,
    val content: String,
    val helpCategory: String,
    val status: String,
    val createdAt: Long,
    val completedAt: Long?
)
