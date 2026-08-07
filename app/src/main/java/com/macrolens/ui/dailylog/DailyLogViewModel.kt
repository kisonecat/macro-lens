package com.macrolens.ui.dailylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.macrolens.data.local.FoodEntry
import com.macrolens.data.local.MacroTotals
import com.macrolens.data.remote.OpenAiClient
import com.macrolens.data.settings.DailyGoals
import com.macrolens.data.settings.SettingsDataStore
import com.macrolens.repository.FoodAnalysisQueue
import com.macrolens.repository.FoodRepository
import com.macrolens.repository.LlmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DailyLogUiState(
    val entries: List<FoodEntry> = emptyList(),
    val totals: MacroTotals = MacroTotals.ZERO,
    val goals: DailyGoals = DailyGoals.NONE,
    val inspirationalMessage: String? = null,
    val isLoadingMessage: Boolean = false,
    val showTextEntry: Boolean = false,
    val isAnalyzingText: Boolean = false,
    val textEntryError: String? = null
)

class DailyLogViewModel(
    private val foodRepository: FoodRepository,
    private val llmRepository: LlmRepository,
    private val analysisQueue: FoodAnalysisQueue,
    private val settingsDataStore: SettingsDataStore
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

        viewModelScope.launch {
            settingsDataStore.goals.collectLatest { goals ->
                _uiState.value = _uiState.value.copy(goals = goals)
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

    fun retryEntry(entry: FoodEntry) {
        viewModelScope.launch {
            foodRepository.markPending(entry.id)
            analysisQueue.kick()
        }
    }

    fun showTextEntry() {
        _uiState.value = _uiState.value.copy(showTextEntry = true, textEntryError = null)
    }

    fun dismissTextEntry() {
        _uiState.value = _uiState.value.copy(
            showTextEntry = false,
            isAnalyzingText = false,
            textEntryError = null
        )
    }

    fun submitTextEntry(description: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzingText = true, textEntryError = null)
            llmRepository.analyzeFoodFromText(description)
                .onSuccess { estimate ->
                    val entry = FoodEntry(
                        date = today,
                        calories = estimate.calories,
                        proteinG = estimate.proteinG,
                        carbsG = estimate.carbsG,
                        fatG = estimate.fatG,
                        fruitVegServings = estimate.fruitVegServings,
                        description = estimate.description
                    )
                    foodRepository.addEntry(entry)
                    _uiState.value = _uiState.value.copy(showTextEntry = false, isAnalyzingText = false)
                }
                .onFailure { error ->
                    val message = when {
                        error.message?.contains("API key") == true -> "Please set your API key in Settings"
                        error is OpenAiClient.NoFoodFoundException -> "Not recognized as food"
                        else -> error.message ?: "Failed to analyze"
                    }
                    _uiState.value = _uiState.value.copy(isAnalyzingText = false, textEntryError = message)
                }
        }
    }

    fun refreshMessage() {
        fetchInspirationalMessage(_uiState.value.totals)
    }

    companion object {
        fun factory(
            foodRepository: FoodRepository,
            llmRepository: LlmRepository,
            analysisQueue: FoodAnalysisQueue,
            settingsDataStore: SettingsDataStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DailyLogViewModel(
                    foodRepository,
                    llmRepository,
                    analysisQueue,
                    settingsDataStore
                ) as T
            }
        }
    }
}
