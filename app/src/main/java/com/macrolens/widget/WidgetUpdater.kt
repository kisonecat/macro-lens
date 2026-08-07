package com.macrolens.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.macrolens.data.local.MacroTotals
import com.macrolens.data.settings.DailyGoals
import com.macrolens.data.settings.SettingsDataStore
import com.macrolens.repository.FoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Observes today's totals and daily goals; whenever either changes, tells all
 * home-screen widget instances to re-render. Also re-anchors to the current day
 * every 15 minutes so the widget rolls over after midnight even without any
 * new writes.
 */
class WidgetUpdater(
    private val context: Context,
    private val foodRepository: FoodRepository,
    private val settingsDataStore: SettingsDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        scope.launch {
            val totalsFlow: Flow<MacroTotals> = currentDateFlow()
                .flatMapLatest { foodRepository.getTotalsForDate(it) }
            val goalsFlow: Flow<DailyGoals> = settingsDataStore.goals

            combine(totalsFlow, goalsFlow) { totals: MacroTotals, goals: DailyGoals ->
                totals to goals
            }.collect {
                runCatching { FoodTotalsWidget().updateAll(context) }
                    .onFailure { Log.w(TAG, "Widget update failed", it) }
            }
        }
    }

    private fun currentDateFlow() = flow {
        while (true) {
            emit(LocalDate.now())
            delay(REFRESH_INTERVAL_MS)
        }
    }.distinctUntilChanged()

    companion object {
        private const val TAG = "WidgetUpdater"
        private const val REFRESH_INTERVAL_MS = 15 * 60 * 1000L
    }
}
