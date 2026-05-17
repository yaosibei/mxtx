package com.mindeye.app.core.feedback

import com.mindeye.app.di.TextToSpeechManager
import com.mindeye.app.core.feedback.VibrationManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局反馈管理器。
 * 后续成员 2/3/4 不要直接散落调用 TTS 和震动，统一走这里。
 */
@Singleton
class FeedbackManager @Inject constructor(
    private val ttsManager: TextToSpeechManager,
    private val vibrationManager: VibrationManager
) {
    fun notify(message: String, priority: FeedbackPriority = FeedbackPriority.NORMAL) {
        when (priority) {
            FeedbackPriority.LOW -> ttsManager.speak(message)
            FeedbackPriority.NORMAL -> ttsManager.speak(message)
            FeedbackPriority.HIGH -> {
                vibrationManager.vibrateStrong()
                ttsManager.speak(message)
            }
            FeedbackPriority.CRITICAL -> {
                vibrationManager.vibrateEmergency()
                ttsManager.speak(message)
            }
        }
    }
}

enum class FeedbackPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}
