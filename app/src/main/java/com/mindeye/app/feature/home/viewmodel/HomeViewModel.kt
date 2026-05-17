package com.mindeye.app.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.di.TextToSpeechManager
import com.mindeye.app.core.model.SafetyLevel
import com.mindeye.app.core.model.SceneType
import com.mindeye.app.core.model.GetRecentScenesUseCase
import com.mindeye.app.core.model.SaveSceneRecordUseCase
import com.mindeye.app.core.database.entity.SceneRecordEntity
import com.mindeye.app.core.sensor.AudioLevelListener
import com.mindeye.app.core.sensor.AppSensorManager
import com.mindeye.app.core.feedback.VibrationManager
import com.mindeye.app.core.location.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val sceneType: SceneType = SceneType.UNKNOWN,
    val safetyLevel: SafetyLevel = SafetyLevel.SAFE,
    val recommendations: List<String> = emptyList(),
    val isSceneAnalysisRunning: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecentScenesUseCase: GetRecentScenesUseCase,
    private val saveSceneRecordUseCase: SaveSceneRecordUseCase,
    private val audioLevelListener: AudioLevelListener,
    private val appSensorManager: AppSensorManager,
    private val vibrationManager: VibrationManager,
    private val ttsManager: TextToSpeechManager,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecentScenes()
    }

    fun startSceneAnalysis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSceneAnalysisRunning = true, isLoading = false)
            audioLevelListener.startListening()
            appSensorManager.startListening()
            ttsManager.speak("场景分析已启动")
        }
    }

    fun stopSceneAnalysis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSceneAnalysisRunning = false)
            audioLevelListener.stopListening()
            appSensorManager.stopListening()
            ttsManager.speak("场景分析已停止")
        }
    }

    fun analyzeCurrentScene() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val audioLevel = audioLevelListener.getCurrentAudioLevel().toFloat()
            val isMoving = appSensorManager.isMoving()
            val motionState = appSensorManager.detectMotionState()
            val location = locationService.getLastLocation()

            val sceneType = classifyScene(audioLevel, isMoving, motionState)
            val recommendations = getRecommendations(sceneType)
            val safetyLevel = getSafetyLevel(sceneType)

            applySceneStrategy(sceneType, safetyLevel)

            val record = SceneRecordEntity(
                sceneType = sceneType.name.lowercase(),
                sceneSubtype = sceneType.name,
                description = recommendations.joinToString(","),
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                timestamp = System.currentTimeMillis(),
                audioLevel = audioLevel.toDouble(),
                vibrationPattern = when (safetyLevel) {
                    SafetyLevel.DANGER -> 2
                    SafetyLevel.CAUTION -> 1
                    SafetyLevel.SAFE -> 0
                },
                voiceEnabled = true
            )
            saveSceneRecordUseCase(record)

            _uiState.value = _uiState.value.copy(
                sceneType = sceneType,
                safetyLevel = safetyLevel,
                recommendations = recommendations,
                isLoading = false
            )

            ttsManager.speak(recommendations.firstOrNull() ?: "场景分析完成")
        }
    }

    private fun classifyScene(audioLevel: Float, isMoving: Boolean, motionState: String): SceneType {
        return when {
            audioLevel > 65f && motionState == "walking" -> SceneType.SOCIAL
            audioLevel > 60f && motionState == "vehicle" -> SceneType.TRAFFIC
            audioLevel < 40f && !isMoving -> SceneType.INDOOR_QUIET
            audioLevel > 60f && !isMoving -> SceneType.INDOOR_NOISY
            audioLevel < 50f && isMoving -> SceneType.OUTDOOR_QUIET
            audioLevel > 70f && isMoving -> SceneType.OUTDOOR_NOISY
            else -> SceneType.INDOOR_QUIET
        }
    }

    private fun getRecommendations(sceneType: SceneType): List<String> = when (sceneType) {
        SceneType.INDOOR_QUIET -> listOf("环境安静，适合语音交互")
        SceneType.INDOOR_NOISY -> listOf("环境嘈杂，建议使用震动反馈")
        SceneType.OUTDOOR_QUIET -> listOf("户外环境安静，注意周围安全")
        SceneType.OUTDOOR_NOISY -> listOf("户外环境嘈杂，建议使用震动导航")
        SceneType.SOCIAL -> listOf("检测到社交场景，注意周围人流")
        SceneType.TRAFFIC -> listOf("检测到交通场景，注意交通安全")
        SceneType.UNKNOWN -> listOf("请小心行走")
    }

    private fun getSafetyLevel(sceneType: SceneType): SafetyLevel = when (sceneType) {
        SceneType.TRAFFIC -> SafetyLevel.CAUTION
        SceneType.OUTDOOR_NOISY -> SafetyLevel.CAUTION
        else -> SafetyLevel.SAFE
    }

    private fun applySceneStrategy(sceneType: SceneType, safetyLevel: SafetyLevel) {
        when (sceneType) {
            SceneType.INDOOR_NOISY, SceneType.OUTDOOR_NOISY -> vibrationManager.vibrateStrong()
            SceneType.TRAFFIC -> vibrationManager.vibrateForIntersection()
            else -> {}
        }
        when (safetyLevel) {
            SafetyLevel.DANGER -> vibrationManager.vibrateEmergency()
            SafetyLevel.CAUTION -> vibrationManager.vibrateStrong()
            SafetyLevel.SAFE -> {}
        }
    }

    private fun loadRecentScenes() {
        viewModelScope.launch {
            try {
                val scenes = getRecentScenesUseCase(5)
                if (scenes.isNotEmpty()) {
                    val latest = scenes.first()
                    _uiState.value = _uiState.value.copy(
                        sceneType = SceneType.valueOf(latest.sceneType.uppercase()),
                        recommendations = latest.description.split(",")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioLevelListener.stopListening()
        appSensorManager.stopListening()
    }
}
