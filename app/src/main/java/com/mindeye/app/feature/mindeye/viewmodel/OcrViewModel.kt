package com.mindeye.app.feature.mindeye.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.di.TextToSpeechManager
import com.mindeye.app.feature.mindeye.vision.PerformOcrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OcrUiState(
    val recognizedText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val performOcrUseCase: PerformOcrUseCase,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun performOcr(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = performOcrUseCase(bitmap)
                _uiState.value = _uiState.value.copy(
                    recognizedText = result.text,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "OCR 识别失败"
                )
            }
        }
    }

    fun speakRecognizedText() {
        val text = _uiState.value.recognizedText
        if (text.isNotEmpty()) {
            ttsManager.speak(text)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        performOcrUseCase.close()
    }
}
