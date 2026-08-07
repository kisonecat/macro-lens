package com.macrolens.di

import android.content.Context
import com.macrolens.data.local.PhoodDatabase
import com.macrolens.data.remote.OpenAiClient
import com.macrolens.data.settings.SettingsDataStore
import com.macrolens.repository.FoodAnalysisQueue
import com.macrolens.repository.FoodRepository
import com.macrolens.repository.LlmRepository
import com.macrolens.repository.ThumbnailRepository
import com.macrolens.widget.WidgetUpdater

class AppContainer(context: Context) {

    private val database = PhoodDatabase.getInstance(context)
    private val dailyLogDao = database.dailyLogDao()
    private val openAiClient = OpenAiClient()

    val settingsDataStore = SettingsDataStore(context)
    val foodRepository = FoodRepository(dailyLogDao)
    val llmRepository = LlmRepository(openAiClient, settingsDataStore)
    val thumbnailRepository = ThumbnailRepository(context)
    val foodAnalysisQueue = FoodAnalysisQueue(foodRepository, llmRepository).also { it.start() }
    val widgetUpdater = WidgetUpdater(context, foodRepository, settingsDataStore).also { it.start() }
}
