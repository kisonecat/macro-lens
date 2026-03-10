package com.phood.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phood.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val apiKey: StateFlow<String> = settingsDataStore.apiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val customPrompt: StateFlow<String> = settingsDataStore.customPrompt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_PROMPT)

    fun updateApiKey(key: String) {
        viewModelScope.launch {
            settingsDataStore.setApiKey(key)
        }
    }

    fun updateCustomPrompt(prompt: String) {
        viewModelScope.launch {
            settingsDataStore.setCustomPrompt(prompt)
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
