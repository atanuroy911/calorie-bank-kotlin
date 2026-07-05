package com.roy.caloriebank.widget

import android.content.Context
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.roy.caloriebank.MainActivity
import dagger.hilt.android.EntryPointAccessors
import java.time.Instant
import kotlinx.coroutines.flow.first

private val WidgetBackground = Color(0xFF10171A)
private val WidgetTextMuted = Color(0xFF9AABAC)
private val WidgetTextPrimary = Color(0xFFF1F6F5)
private val WidgetPrimary = Color(0xFF1FD9A8)
private val WidgetNegative = Color(0xFFFF6B6B)
private val WidgetInfo = Color(0xFF6FCBEF)

/** Home-screen widget: today's calorie budget / consumed / remaining, tap to open the app. */
class BalanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val userId = entryPoint.preferencesRepository().userId.first()
        val summary = userId?.let { entryPoint.dailySummaryRepository().getSummaryForDate(it, Instant.now()) }

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
                        "Today's Budget",
                        style = TextStyle(color = ColorProvider(WidgetTextMuted), fontSize = 12.sp),
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    if (summary == null) {
                        Text(
                            "Open Calorie Bank to get started",
                            style = TextStyle(color = ColorProvider(WidgetTextPrimary), fontSize = 14.sp),
                        )
                    } else {
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            StatColumn("Remaining", summary.remaining, WidgetPrimary, GlanceModifier.width(90.dp))
                            StatColumn("Consumed", summary.consumed, WidgetNegative, GlanceModifier.width(90.dp))
                            StatColumn("Budget", summary.totalAvailable, WidgetInfo, GlanceModifier.width(90.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: Int, color: Color, modifier: GlanceModifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Horizontal.Start) {
        Text("$value", style = TextStyle(color = ColorProvider(color), fontSize = 20.sp, fontWeight = FontWeight.Bold))
        Text(label, style = TextStyle(color = ColorProvider(WidgetTextMuted), fontSize = 11.sp))
    }
}

class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()
}
