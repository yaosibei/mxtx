package com.mindeye.app.feature.mindeye.domain.model

/**
 * 明心之眼出行模块的数据结构。
 * 成员 2 负责填充 PreTripVisionReport；成员 4 负责根据报告生成 TravelPlan。
 */
data class Destination(
    val name: String,
    val type: DestinationType
)

enum class DestinationType {
    HOME,
    TRAIN_STATION,
    AIRPORT,
    HOSPITAL,
    COMMUNITY_SERVICE,
    CUSTOM
}

data class PreTripVisionReport(
    val placeType: PlaceType,
    val recognizedTexts: List<String>,
    val detectedObjects: List<String>,
    val risks: List<TravelRisk>,
    val summary: String
)

enum class PlaceType {
    INDOOR,
    OUTDOOR,
    UNKNOWN
}

data class TravelRisk(
    val type: TravelRiskType,
    val level: RiskLevel,
    val description: String,
    val suggestion: String
)

enum class TravelRiskType {
    STAIRS,
    VEHICLE,
    CROWD,
    OBSTACLE,
    GLASS_DOOR,
    CONSTRUCTION,
    GPS_WEAK,
    NOISY_ENVIRONMENT
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class TravelPlan(
    val destination: Destination,
    val preTripSummary: String,
    val routeSummary: String,
    val risks: List<TravelRisk>,
    val recommendedActions: List<String>,
    val needStaffAssist: Boolean,
    val voiceSummary: String
)
