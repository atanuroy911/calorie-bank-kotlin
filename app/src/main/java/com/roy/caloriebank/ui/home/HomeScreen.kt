package com.roy.caloriebank.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.FoodEntry
import com.roy.caloriebank.domain.model.icon
import com.roy.caloriebank.domain.model.isPositive
import com.roy.caloriebank.domain.model.label
import com.roy.caloriebank.domain.util.percentOf
import com.roy.caloriebank.domain.util.signedKcal
import com.roy.caloriebank.domain.util.timeLabel
import com.roy.caloriebank.ui.theme.AccentGradient
import com.roy.caloriebank.ui.theme.AiBannerGradient
import com.roy.caloriebank.ui.theme.BalanceCardGradient
import com.roy.caloriebank.ui.theme.CardBorderColor
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.PositiveColor
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.TextMutedColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

@Composable
fun HomeScreen(
    onOpenChat: () -> Unit,
    onOpenNutrition: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenManualLog: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val greeting by viewModel.greeting.collectAsStateWithLifecycle()

    Scaffold(
        // A docked bottom bar (rather than a floating FAB) so Scaffold's content padding always
        // reserves space for it — a floating FAB does not affect content padding and would overlap
        // the last list item whenever the screen's content is shorter than the viewport.
        bottomBar = {
            androidx.compose.material3.Surface(color = com.roy.caloriebank.ui.theme.BackgroundColor) {
                ExtendedFloatingActionButton(
                    onClick = onOpenManualLog,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                ) {
                    Text("Manual Log")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(greeting, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            uiState.profile?.displayName ?: "Calorie Banker",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentGradient)
                            .clickable { onOpenProfile() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = "Profile", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }

            if (uiState.currentStreak > 0) {
                item { StreakChip(uiState.currentStreak) }
            }

            uiState.summary?.let { summary ->
                item {
                    BalanceCard(
                        totalAvailable = summary.totalAvailable,
                        consumed = summary.consumed,
                        remaining = summary.remaining,
                        bankBalance = uiState.bankAccount?.balance ?: 0,
                        consumedPercent = summary.consumedPercent,
                        isOverBudget = summary.isOverBudget,
                    )
                }
            }

            if (uiState.summary != null && uiState.profile != null) {
                item {
                    MacroProgressCard(
                        summary = uiState.summary!!,
                        profile = uiState.profile!!,
                        onClick = onOpenNutrition,
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AiBannerGradient)
                        .clickable { onOpenChat() }
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = PrimaryColor)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Log with CalBot", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Just tell me what you ate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryColor,
                            )
                        }
                    }
                }
            }

            if (uiState.todaysFoodEntries.isNotEmpty()) {
                item { Text("Today's Meals", style = MaterialTheme.typography.titleLarge) }
                items(uiState.todaysFoodEntries) { entry -> FoodEntryTile(entry) }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Transactions", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryColor,
                        modifier = Modifier.clickable { onOpenTransactions() },
                    )
                }
            }
            items(uiState.todaysTransactions.take(5)) { tx -> TransactionTile(tx) }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun StreakChip(streak: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(com.roy.caloriebank.ui.theme.WarningColor.copy(alpha = 0.16f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🔥", style = MaterialTheme.typography.titleMedium)
        Text(
            "$streak day${if (streak == 1) "" else "s"} under budget",
            style = MaterialTheme.typography.labelLarge,
            color = com.roy.caloriebank.ui.theme.WarningColor,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun BalanceCard(
    totalAvailable: Int,
    consumed: Int,
    remaining: Int,
    bankBalance: Int,
    consumedPercent: Double,
    isOverBudget: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BalanceCardGradient)
            .padding(16.dp),
    ) {
        Text("Today's Account", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BalanceStat("Daily Budget", totalAvailable, PrimaryColor, Modifier.weight(1f))
            BalanceStat("Consumed", consumed, NegativeColor, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BalanceStat(
                "Remaining",
                remaining,
                if (isOverBudget) NegativeColor else PositiveColor,
                Modifier.weight(1f),
            )
            BalanceStat("Bank Balance", bankBalance, com.roy.caloriebank.ui.theme.AccentColor, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        val progress = consumedPercent.toFloat().coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CardBorderColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isOverBudget) NegativeColor else PrimaryColor),
            )
        }
    }
}

@Composable
private fun BalanceStat(label: String, value: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text("$value kcal", style = MaterialTheme.typography.titleLarge, color = color)
    }
}

@Composable
private fun FoodEntryTile(entry: FoodEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.mealTypeLabel, style = MaterialTheme.typography.titleMedium)
            Text(
                entry.foods.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryColor,
            )
        }
        Text("-${entry.totalCalories} kcal", style = MaterialTheme.typography.bodyMedium, color = NegativeColor)
    }
}

@Composable
private fun TransactionTile(tx: CalorieTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(tx.type.icon, style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(tx.label, style = MaterialTheme.typography.titleMedium)
                Text(tx.timestamp.timeLabel(), style = MaterialTheme.typography.bodySmall, color = TextSecondaryColor)
            }
        }
        Text(
            tx.signedCalories.signedKcal(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (tx.type.isPositive) PositiveColor else NegativeColor,
        )
    }
}
