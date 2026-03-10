package com.phood.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val API_KEY = stringPreferencesKey("openai_api_key")
        val CUSTOM_PROMPT = stringPreferencesKey("custom_prompt")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.API_KEY] ?: ""
    }

    val customPrompt: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_PROMPT] ?: DEFAULT_PROMPT
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_KEY] = key
        }
    }

    suspend fun setCustomPrompt(prompt: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CUSTOM_PROMPT] = prompt
        }
    }

    companion object {
        const val DEFAULT_PROMPT = "Give me a short, encouraging message about my eating today."
    }
}
