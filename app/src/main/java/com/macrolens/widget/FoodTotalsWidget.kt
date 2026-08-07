package com.macrolens.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.macrolens.MainActivity
import com.macrolens.PhoodApplication
import com.macrolens.data.local.MacroTotals
import com.macrolens.data.settings.DailyGoals
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class FoodTotalsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as PhoodApplication).container
        val totals = container.foodRepository.getTotalsForDate(LocalDate.now()).first()
        val goals = container.settingsDataStore.goals.first()

        provideContent {
            GlanceTheme {
                WidgetContent(context, totals, goals)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, totals: MacroTotals, goals: DailyGoals) {
        val openLogIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_START_ROUTE, MainActivity.ROUTE_DAILY_LOG)
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp)
                .padding(8.dp)
                .clickable(actionStartActivity(openLogIntent)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${totals.calories}",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            if (goals.calories > 0) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                val progress = (totals.calories.toFloat() / goals.calories.toFloat())
                    .coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = progress,
                    modifier = GlanceModifier.fillMaxWidth().height(3.dp),
                    color = if (totals.calories > goals.calories)
                        ColorProvider(Color(0xFFB58900))
                    else GlanceTheme.colors.primary,
                    backgroundColor = GlanceTheme.colors.surfaceVariant
                )
            }
        }
    }
}
