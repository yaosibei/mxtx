package com.mindeye.app.feature.senseflow.domain

/**
 * 随境 SenseFlow 的状态模型。
 * 它应该在后台轻量运行，持续判断环境，再把提醒策略交给 FeedbackManager。
 */
data class SenseFlowState(
    val sceneType: SenseSceneType = SenseSceneType.UNKNOWN,
    val noiseLevel: NoiseLevel = NoiseLevel.UNKNOWN,
    val motionState: MotionState = MotionState.UNKNOWN,
    val feedbackStrategy: FeedbackStrategy = FeedbackStrategy.VOICE_MAIN,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SenseSceneType {
    INDOOR_QUIET,
    INDOOR_NOISY,
    OUTDOOR_QUIET,
    OUTDOOR_NOISY,
    SOCIAL,
    TRAFFIC,
    UNKNOWN
}

enum class NoiseLevel { QUIET, NORMAL, NOISY, UNKNOWN }

enum class MotionState { STILL, WALKING, RUNNING, VEHICLE, UNKNOWN }

enum class FeedbackStrategy {
    VOICE_MAIN,
    VIBRATION_MAIN,
    SHORT_VOICE_WITH_VIBRATION,
    SILENT_SCREEN,
    EMERGENCY_INTERRUPT
}
