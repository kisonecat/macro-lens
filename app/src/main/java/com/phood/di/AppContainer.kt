package com.phood.di

import android.content.Context
import com.phood.data.local.PhoodDatabase
import com.phood.data.remote.OpenAiClient
import com.phood.data.settings.SettingsDataStore
import com.phood.repository.FoodRepository
import com.phood.repository.LlmRepository

class AppContainer(context: Context) {

    private val database = PhoodDatabase.getInstance(context)
    private val dailyLogDao = database.dailyLogDao()
    private val openAiClient = OpenAiClient()

    val settingsDataStore = SettingsDataStore(context)
    val foodRepository = FoodRepository(dailyLogDao)
    val llmRepository = LlmRepository(openAiClient, settingsDataStore)
}
