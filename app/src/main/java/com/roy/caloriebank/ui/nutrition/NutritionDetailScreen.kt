package com.roy.caloriebank.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.domain.model.DailyReferenceValues
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.UserProfile
import com.roy.caloriebank.domain.util.percentOf
import com.roy.caloriebank.ui.theme.CarbsColor
import com.roy.caloriebank.ui.theme.CholesterolColor
import com.roy.caloriebank.ui.theme.FatColor
import com.roy.caloriebank.ui.theme.FiberRingColor
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.PrimaryGradient
import com.roy.caloriebank.ui.theme.ProteinColor
import com.roy.caloriebank.ui.theme.SatFatColor
import com.roy.caloriebank.ui.theme.SugarColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor
import com.roy.caloriebank.ui.theme.TransFatColor

private data class MacroTileData(val label: String, val value: Double, val goal: Double, val color: Color, val unit: String = "g")

@Composable
fun NutritionDetailScreen(viewModel: NutritionDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = uiState.summary
    val profile = uiState.profile

    if (summary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("🥗 No food logged today", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { CalorieSummaryPill(summary) }

        item { Text("Macronutrients", style = MaterialTheme.typography.titleLarge) }
        item {
            val tiles = listOf(
                MacroTileData("Protein", summary.macros.proteinG, profile?.dailyProteinGoalG ?: 0.0, ProteinColor),
                MacroTileData("Carbs", summary.macros.carbsG, profile?.dailyCarbsGoalG ?: 0.0, CarbsColor),
                MacroTileData("Fat", summary.macros.fatG, profile?.dailyFatGoalG ?: 0.0, FatColor),
                MacroTileData("Fiber", summary.macros.fiberG, DailyReferenceValues.fiberG, FiberRingColor),
                MacroTileData("Sugar", summary.macros.sugarG, DailyReferenceValues.sugarG, SugarColor),
                MacroTileData("Sat. Fat", summary.macros.saturatedFatG, DailyReferenceValues.saturatedFatG, SatFatColor),
                MacroTileData("Trans Fat", summary.macros.transFatG, 0.0, TransFatColor),
                MacroTileData("Cholesterol", summary.macros.cholesterolMg, DailyReferenceValues.cholesterolMg, CholesterolColor, "mg"),
            )
            MacroGrid(tiles)
        }

        item { Text("Minerals", style = MaterialTheme.typography.titleLarge) }
        item {
            MicroList(
                listOf(
                    MicroRowData("Sodium", summary.micros.sodiumMg, DailyReferenceValues.sodiumMg, invert = true),
                    MicroRowData("Potassium", summary.micros.potassiumMg, DailyReferenceValues.potassiumMg),
                    MicroRowData("Calcium", summary.micros.calciumMg, DailyReferenceValues.calciumMg),
                    MicroRowData("Iron", summary.micros.ironMg, DailyReferenceValues.ironMg),
                    MicroRowData("Magnesium", summary.micros.magnesiumMg, DailyReferenceValues.magnesiumMg),
                    MicroRowData("Zinc", summary.micros.zincMg, DailyReferenceValues.zincMg),
                    MicroRowData("Phosphorus", summary.micros.phosphorusMg, DailyReferenceValues.phosphorusMg),
                ),
            )
        }

        item { Text("Vitamins", style = MaterialTheme.typography.titleLarge) }
        item {
            MicroList(
                listOf(
                    MicroRowData("Vitamin C", summary.micros.vitaminCMg, DailyReferenceValues.vitaminCMg),
                    MicroRowData("Vitamin D", summary.micros.vitaminDUg, DailyReferenceValues.vitaminDUg),
                    MicroRowData("Vitamin B12", summary.micros.vitaminB12Ug, DailyReferenceValues.vitaminB12Ug),
                    MicroRowData("Folate", summary.micros.folateMcg, DailyReferenceValues.folateMcg),
                    MicroRowData("Vitamin A", summary.micros.vitaminAUg, DailyReferenceValues.vitaminAUg),
                    MicroRowData("Vitamin E", summary.micros.vitaminEMg, DailyReferenceValues.vitaminEMg),
                    MicroRowData("Vitamin K", summary.micros.vitaminKUg, DailyReferenceValues.vitaminKUg),
                ),
            )
        }
    }
}

@Composable
private fun CalorieSummaryPill(summary: DailySummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PrimaryGradient)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        PillStat("Consumed", summary.consumed)
        PillStat("Remaining", summary.remaining)
        PillStat("Budget", summary.totalAvailable)
    }
}

@Composable
private fun PillStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.titleLarge, color = TextOnPrimaryColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextOnPrimaryColor)
    }
}

@Composable
private fun MacroGrid(tiles: List<MacroTileData>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(100.dp * (tiles.size / 2 + tiles.size % 2)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tiles) { tile -> MacroTile(tile) }
    }
}

@Composable
private fun MacroTile(tile: MacroTileData) {
    val progress = if (tile.goal > 0) (tile.value / tile.goal).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOver = tile.goal > 0 && tile.value > tile.goal * 1.05
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(tile.label, style = MaterialTheme.typography.titleSmall)
            if (isOver) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = NegativeColor, modifier = Modifier.height(16.dp))
            }
        }
        Text("${"%.1f".format(tile.value)}${tile.unit}", style = MaterialTheme.typography.titleMedium)
        if (tile.goal > 0) {
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = tile.color,
            )
        }
    }
}

private data class MicroRowData(val label: String, val value: Double, val rdv: Double, val invert: Boolean = false)

@Composable
private fun MicroList(rows: List<MicroRowData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(12.dp),
    ) {
        rows.forEachIndexed { index, row -> MicroRow(row); if (index != rows.lastIndex) Spacer(Modifier.height(10.dp)) }
    }
}

@Composable
private fun MicroRow(row: MicroRowData) {
    val progress = if (row.rdv > 0) (row.value / row.rdv).coerceIn(0.0, 1.2).toFloat() else 0f
    val percent = row.value.percentOf(row.rdv)
    val isWarning = if (row.invert) row.value > row.rdv * 1.1 else false
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(row.label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${"%.1f".format(row.value)} · ${percent.toInt()}% DV",
                style = MaterialTheme.typography.bodySmall,
                color = if (isWarning) NegativeColor else TextSecondaryColor,
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress / 1.2f },
            modifier = Modifier.fillMaxWidth(),
            color = if (isWarning) NegativeColor else MaterialTheme.colorScheme.primary,
        )
    }
}
