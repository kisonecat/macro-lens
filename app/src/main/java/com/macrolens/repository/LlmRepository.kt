package com.macrolens.repository

import com.macrolens.data.local.MacroTotals
import com.macrolens.data.remote.FoodEstimate
import com.macrolens.data.remote.OpenAiClient
import com.macrolens.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.first

class LlmRepository(
    private val client: OpenAiClient,
    private val settings: SettingsDataStore
) {

    suspend fun analyzeFoodFromText(description: String): Result<FoodEstimate> {
        val apiKey = settings.apiKey.first()
        if (apiKey.isBlank()) return Result.failure(IllegalStateException("API key not configured"))
        return client.analyzeFoodFromText(apiKey, description)
    }

    suspend fun analyzeFood(imageBase64: String): Result<FoodEstimate> {
        val apiKey = settings.apiKey.first()
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("API key not configured"))
        }
        return client.analyzeFood(apiKey, imageBase64)
    }

    suspend fun getInspirationalMessage(totals: MacroTotals): Result<String> {
        val apiKey = settings.apiKey.first()
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("API key not configured"))
        }

        val customPrompt = settings.customPrompt.first()

        return client.getInspirationalMessage(
            apiKey = apiKey,
            customPrompt = customPrompt,
            calories = totals.calories,
            proteinG = totals.proteinG,
            carbsG = totals.carbsG,
            fatG = totals.fatG
        )
    }
}
