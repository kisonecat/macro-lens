package com.phood.ui.camera

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phood.data.local.FoodEntry
import com.phood.repository.FoodRepository
import com.phood.repository.LlmRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CameraUiState(
    val isProcessing: Boolean = false,
    val error: String? = null
)

sealed class CameraEvent {
    data object NavigateToDailyLog : CameraEvent()
    data object NavigateToSettings : CameraEvent()
}

class CameraViewModel(
    private val foodRepository: FoodRepository,
    private val llmRepository: LlmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CameraEvent>()
    val events: SharedFlow<CameraEvent> = _events.asSharedFlow()

    fun onImageCaptured(jpegBytes: ByteArray) {
        if (_uiState.value.isProcessing) return

        viewModelScope.launch {
            try {
                _uiState.value = CameraUiState(isProcessing = true)

                val base64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)

                llmRepository.analyzeFood(base64)
                    .onSuccess { estimate ->
                        val entry = FoodEntry(
                            date = LocalDate.now(),
                            calories = estimate.calories,
                            proteinG = estimate.proteinG,
                            carbsG = estimate.carbsG,
                            fatG = estimate.fatG,
                            description = estimate.description
                        )
                        foodRepository.addEntry(entry)
                        _uiState.value = CameraUiState(isProcessing = false)
                        _events.emit(CameraEvent.NavigateToDailyLog)
                    }
                    .onFailure { error ->
                        val message = when {
                            error.message?.contains("API key") == true -> "Please set your API key in Settings"
                            error is com.phood.data.remote.OpenAiClient.NoFoodFoundException ->
                                "No food detected: ${error.message}"
                            else -> error.message ?: "Failed to analyze food"
                        }
                        _uiState.value = CameraUiState(isProcessing = false, error = message)

                        if (message.contains("API key")) {
                            _events.emit(CameraEvent.NavigateToSettings)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = CameraUiState(
                    isProcessing = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(isProcessing = false, error = message)
    }

    companion object {
        fun factory(
            foodRepository: FoodRepository,
            llmRepository: LlmRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CameraViewModel(foodRepository, llmRepository) as T
            }
        }
    }
}
