package com.phood.ui.dailylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phood.data.local.FoodEntry
import com.phood.data.local.MacroTotals
import com.phood.repository.FoodRepository
import com.phood.repository.LlmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DailyLogUiState(
    val entries: List<FoodEntry> = emptyList(),
    val totals: MacroTotals = MacroTotals.ZERO,
    val inspirationalMessage: String? = null,
    val isLoadingMessage: Boolean = false
)

class DailyLogViewModel(
    private val foodRepository: FoodRepository,
    private val llmRepository: LlmRepository
) : ViewModel() {

    private val today = LocalDate.now()

    private val _uiState = MutableStateFlow(DailyLogUiState())
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private var lastMessageTotals: MacroTotals? = null

    init {
        viewModelScope.launch {
            foodRepository.getEntriesForDate(today).collectLatest { entries ->
                _uiState.value = _uiState.value.copy(entries = entries)
            }
        }

        viewModelScope.launch {
            foodRepository.getTotalsForDate(today).collectLatest { totals ->
                _uiState.value = _uiState.value.copy(totals = totals)

                // Fetch new inspirational message when totals change significantly
                if (shouldRefreshMessage(totals)) {
                    fetchInspirationalMessage(totals)
                }
            }
        }
    }

    private fun shouldRefreshMessage(newTotals: MacroTotals): Boolean {
        val last = lastMessageTotals ?: return true

        // Refresh if calories changed by more than 100
        return kotlin.math.abs(newTotals.calories - last.calories) > 100
    }

    private fun fetchInspirationalMessage(totals: MacroTotals) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMessage = true)

            llmRepository.getInspirationalMessage(totals)
                .onSuccess { message ->
                    lastMessageTotals = totals
                    _uiState.value = _uiState.value.copy(
                        inspirationalMessage = message,
                        isLoadingMessage = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoadingMessage = false)
                }
        }
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            foodRepository.deleteEntry(entry)
        }
    }

    fun refreshMessage() {
        fetchInspirationalMessage(_uiState.value.totals)
    }

    companion object {
        fun factory(
            foodRepository: FoodRepository,
            llmRepository: LlmRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DailyLogViewModel(foodRepository, llmRepository) as T
            }
        }
    }
}
