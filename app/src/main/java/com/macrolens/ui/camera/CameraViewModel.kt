package com.macrolens.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.macrolens.data.local.EntryStatus
import com.macrolens.data.local.FoodEntry
import com.macrolens.repository.FoodRepository
import com.macrolens.repository.ThumbnailRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CameraUiState(
    val error: String? = null
)

sealed class CameraEvent {
    data object NavigateToDailyLog : CameraEvent()
    data object NavigateToSettings : CameraEvent()
}

class CameraViewModel(
    private val foodRepository: FoodRepository,
    private val thumbnailRepository: ThumbnailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CameraEvent>()
    val events: SharedFlow<CameraEvent> = _events.asSharedFlow()

    fun onImageCaptured(jpegBytes: ByteArray) {
        viewModelScope.launch {
            val imagePath = thumbnailRepository.saveImage(jpegBytes)
            val thumbnailPath = thumbnailRepository.saveThumbnail(jpegBytes)

            if (imagePath == null) {
                _uiState.value = CameraUiState(error = "Could not save photo")
                return@launch
            }

            foodRepository.addEntry(
                FoodEntry(
                    date = LocalDate.now(),
                    description = "Analyzing…",
                    thumbnailPath = thumbnailPath,
                    imagePath = imagePath,
                    status = EntryStatus.PENDING
                )
            )
            _events.emit(CameraEvent.NavigateToDailyLog)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    companion object {
        fun factory(
            foodRepository: FoodRepository,
            thumbnailRepository: ThumbnailRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CameraViewModel(foodRepository, thumbnailRepository) as T
            }
        }
    }
}
