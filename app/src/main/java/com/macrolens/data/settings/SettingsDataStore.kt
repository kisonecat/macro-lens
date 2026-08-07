package com.macrolens.data.settings

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(
    private val context: Context,
    private val secureKeyStore: SecureKeyStore = SecureKeyStore()
) {

    private object Keys {
        val API_KEY = stringPreferencesKey("openai_api_key")
        val CUSTOM_PROMPT = stringPreferencesKey("custom_prompt")
        val GOAL_CALORIES = intPreferencesKey("goal_calories")
        val GOAL_PROTEIN_G = intPreferencesKey("goal_protein_g")
        val GOAL_CARBS_G = intPreferencesKey("goal_carbs_g")
        val GOAL_FAT_G = intPreferencesKey("goal_fat_g")
        val GOAL_FRUIT_VEG_SERVINGS = intPreferencesKey("goal_fruit_veg_servings")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        val stored = prefs[Keys.API_KEY].orEmpty()
        if (stored.isBlank()) return@map ""
        if (!secureKeyStore.isEncrypted(stored)) {
            // Legacy plaintext value written before Keystore encryption was added.
            // Return it; the next save will upgrade it in place.
            return@map stored
        }
        runCatching { secureKeyStore.decrypt(stored) }
            .onFailure { Log.w(TAG, "Failed to decrypt API key; treating as unset", it) }
            .getOrDefault("")
    }

    val customPrompt: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_PROMPT] ?: DEFAULT_PROMPT
    }

    val goals: Flow<DailyGoals> = context.dataStore.data.map { prefs ->
        DailyGoals(
            calories = prefs[Keys.GOAL_CALORIES] ?: 0,
            proteinG = prefs[Keys.GOAL_PROTEIN_G] ?: 0,
            carbsG = prefs[Keys.GOAL_CARBS_G] ?: 0,
            fatG = prefs[Keys.GOAL_FAT_G] ?: 0,
            fruitVegServings = prefs[Keys.GOAL_FRUIT_VEG_SERVINGS] ?: 0
        )
    }

    suspend fun setApiKey(key: String) {
        val toStore = if (key.isBlank()) "" else secureKeyStore.encrypt(key)
        context.dataStore.edit { prefs ->
            prefs[Keys.API_KEY] = toStore
        }
    }

    suspend fun setCustomPrompt(prompt: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CUSTOM_PROMPT] = prompt
        }
    }

    suspend fun setGoals(goals: DailyGoals) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GOAL_CALORIES] = goals.calories.coerceAtLeast(0)
            prefs[Keys.GOAL_PROTEIN_G] = goals.proteinG.coerceAtLeast(0)
            prefs[Keys.GOAL_CARBS_G] = goals.carbsG.coerceAtLeast(0)
            prefs[Keys.GOAL_FAT_G] = goals.fatG.coerceAtLeast(0)
            prefs[Keys.GOAL_FRUIT_VEG_SERVINGS] = goals.fruitVegServings.coerceAtLeast(0)
        }
    }

    companion object {
        private const val TAG = "SettingsDataStore"
        const val DEFAULT_PROMPT = "Give me a short, encouraging message about my eating today."
    }
}
