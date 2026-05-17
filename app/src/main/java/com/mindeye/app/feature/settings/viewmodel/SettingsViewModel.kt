package com.mindeye.app.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.core.database.dao.SettingsDao
import com.mindeye.app.core.database.entity.SettingsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val elderlyMode: Boolean = true,
    val highContrast: Boolean = false,
    val autoEncouragement: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDao: SettingsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            withContext(Dispatchers.IO) {
                val elderly = settingsDao.getSettingValue("elderly_mode")?.toBoolean() ?: true
                val contrast = settingsDao.getSettingValue("high_contrast")?.toBoolean() ?: false
                val encourage = settingsDao.getSettingValue("auto_encouragement")?.toBoolean() ?: true
                _uiState.value = SettingsUiState(
                    elderlyMode = elderly,
                    highContrast = contrast,
                    autoEncouragement = encourage,
                    isLoading = false
                )
            }
        }
    }

    fun setElderlyMode(value: Boolean) {
        _uiState.value = _uiState.value.copy(elderlyMode = value)
        saveSetting("elderly_mode", value)
    }

    fun setHighContrast(value: Boolean) {
        _uiState.value = _uiState.value.copy(highContrast = value)
        saveSetting("high_contrast", value)
    }

    fun setAutoEncouragement(value: Boolean) {
        _uiState.value = _uiState.value.copy(autoEncouragement = value)
        saveSetting("auto_encouragement", value)
    }

    private fun saveSetting(key: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDao.insertSetting(
                SettingsEntity(key, value.toString(), System.currentTimeMillis())
            )
        }
    }
}
