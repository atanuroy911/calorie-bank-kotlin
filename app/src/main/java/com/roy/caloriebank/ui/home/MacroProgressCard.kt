package com.roy.caloriebank.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roy.caloriebank.domain.model.DailyReferenceValues
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.UserProfile
import com.roy.caloriebank.ui.theme.CardBorderColor
import com.roy.caloriebank.ui.theme.CarbsColor
import com.roy.caloriebank.ui.theme.FatColor
import com.roy.caloriebank.ui.theme.FiberRingColor
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.ProteinColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextMutedColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MacroProgressCard(summary: DailySummary, profile: UserProfile, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Text("Nutrition", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MacroRing("Protein", summary.macros.proteinG, profile.dailyProteinGoalG, ProteinColor)
            MacroRing("Carbs", summary.macros.carbsG, profile.dailyCarbsGoalG, CarbsColor)
            MacroRing("Fat", summary.macros.fatG, profile.dailyFatGoalG, FatColor)
            MacroRing("Fiber", summary.macros.fiberG, DailyReferenceValues.fiberG, FiberRingColor)
        }
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MicroChip("Na", summary.micros.sodiumMg, summary.micros.sodiumMg > DailyReferenceValues.sodiumMg)
            MicroChip("K", summary.micros.potassiumMg, false)
            MicroChip("Ca", summary.micros.calciumMg, false)
            MicroChip("Fe", summary.micros.ironMg, false)
            MicroChip("Vit C", summary.micros.vitaminCMg, false)
        }
    }
}

@Composable
private fun MacroRing(label: String, consumed: Double, goal: Double, color: Color) {
    val progress = if (goal > 0) (consumed / goal).coerceIn(0.0, 1.0).toFloat() else 0f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(56.dp),
                color = CardBorderColor,
                strokeWidth = 5.dp,
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(56.dp),
                color = color,
                strokeWidth = 5.dp,
            )
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            "${consumed.toInt()}g / ${goal.toInt()}g",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryColor,
        )
    }
}

@Composable
private fun MicroChip(label: String, value: Double, isWarning: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isWarning) NegativeColor.copy(alpha = 0.15f) else CardBorderColor.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            "$label ${value.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isWarning) NegativeColor else TextMutedColor,
        )
    }
}
