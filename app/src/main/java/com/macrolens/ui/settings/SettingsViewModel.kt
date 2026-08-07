package com.macrolens.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.macrolens.data.settings.DailyGoals
import com.macrolens.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class GoalsForm(
    val calories: String = "",
    val proteinG: String = "",
    val carbsG: String = "",
    val fatG: String = "",
    val fruitVegServings: String = ""
) {
    fun toGoals(): DailyGoals = DailyGoals(
        calories = calories.toIntOrNull() ?: 0,
        proteinG = proteinG.toIntOrNull() ?: 0,
        carbsG = carbsG.toIntOrNull() ?: 0,
        fatG = fatG.toIntOrNull() ?: 0,
        fruitVegServings = fruitVegServings.toIntOrNull() ?: 0
    )

    companion object {
        fun from(goals: DailyGoals): GoalsForm = GoalsForm(
            calories = goals.calories.takeIf { it > 0 }?.toString().orEmpty(),
            proteinG = goals.proteinG.takeIf { it > 0 }?.toString().orEmpty(),
            carbsG = goals.carbsG.takeIf { it > 0 }?.toString().orEmpty(),
            fatG = goals.fatG.takeIf { it > 0 }?.toString().orEmpty(),
            fruitVegServings = goals.fruitVegServings.takeIf { it > 0 }?.toString().orEmpty()
        )
    }
}

data class SettingsUiState(
    val apiKey: String = "",
    val customPrompt: String = SettingsDataStore.DEFAULT_PROMPT,
    val goals: GoalsForm = GoalsForm(),
    val loaded: Boolean = false,
    val savedApiKey: String = "",
    val savedCustomPrompt: String = SettingsDataStore.DEFAULT_PROMPT,
    val savedGoals: GoalsForm = GoalsForm()
) {
    val isDirty: Boolean
        get() = loaded &&
            (apiKey != savedApiKey || customPrompt != savedCustomPrompt || goals != savedGoals)
}

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _saved = MutableSharedFlow<Unit>()
    val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    init {
        viewModelScope.launch {
            val apiKey = settingsDataStore.apiKey.first()
            val prompt = settingsDataStore.customPrompt.first()
            val goalsForm = GoalsForm.from(settingsDataStore.goals.first())
            _uiState.value = SettingsUiState(
                apiKey = apiKey,
                customPrompt = prompt,
                goals = goalsForm,
                loaded = true,
                savedApiKey = apiKey,
                savedCustomPrompt = prompt,
                savedGoals = goalsForm
            )
        }
    }

    fun onApiKeyChange(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key)
    }

    fun onCustomPromptChange(prompt: String) {
        _uiState.value = _uiState.value.copy(customPrompt = prompt)
    }

    fun onGoalsChange(update: (GoalsForm) -> GoalsForm) {
        _uiState.value = _uiState.value.copy(goals = update(_uiState.value.goals))
    }

    fun save() {
        val current = _uiState.value
        if (!current.isDirty) return
        viewModelScope.launch {
            if (current.apiKey != current.savedApiKey) {
                settingsDataStore.setApiKey(current.apiKey)
            }
            if (current.customPrompt != current.savedCustomPrompt) {
                settingsDataStore.setCustomPrompt(current.customPrompt)
            }
            if (current.goals != current.savedGoals) {
                settingsDataStore.setGoals(current.goals.toGoals())
            }
            _uiState.value = current.copy(
                savedApiKey = current.apiKey,
                savedCustomPrompt = current.customPrompt,
                savedGoals = current.goals
            )
            _saved.emit(Unit)
        }
    }

    companion object {
        fun factory(settingsDataStore: SettingsDataStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(settingsDataStore) as T
                }
            }
    }
}
