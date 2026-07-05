package com.roy.caloriebank.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.roy.caloriebank.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

private val WidgetBackground = Color(0xFF10171A)
private val WidgetTextMuted = Color(0xFF9AABAC)
private val WidgetTextPrimary = Color(0xFFF1F6F5)
private val WidgetPrimary = Color(0xFF1FD9A8)
private val WidgetWarning = Color(0xFFF7B955)

/** Home-screen widget: bank balance (calories saved) + current under-budget streak. */
class BankStreakWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val prefs = entryPoint.preferencesRepository()
        val userId = prefs.userId.first()
        val bankBalance = userId?.let { entryPoint.bankRepository().getBankAccount(it).balance } ?: 0
        val streak = prefs.currentStreak.first()

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(WidgetBackground)
                        .padding(16.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                ) {
                    Text(
                        "Calorie Bank",
                        style = TextStyle(color = ColorProvider(WidgetTextMuted), fontSize = 12.sp),
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    Text(
                        "$bankBalance kcal",
                        style = TextStyle(color = ColorProvider(WidgetPrimary), fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            if (streak > 0) "🔥 $streak day streak" else "Stay under budget to start a streak",
                            style = TextStyle(color = ColorProvider(if (streak > 0) WidgetWarning else WidgetTextMuted), fontSize = 13.sp),
                        )
                    }
                }
            }
        }
    }
}

class BankStreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BankStreakWidget()
}
