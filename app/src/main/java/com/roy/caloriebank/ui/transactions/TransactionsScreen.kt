package com.roy.caloriebank.ui.transactions

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.icon
import com.roy.caloriebank.domain.model.isPositive
import com.roy.caloriebank.domain.util.signedKcal
import com.roy.caloriebank.domain.util.timeLabel
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.PositiveColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard("Earned", uiState.earned, PositiveColor, Modifier.weight(1f))
            SummaryCard("Spent", uiState.spent, NegativeColor, Modifier.weight(1f))
        }

        if (uiState.transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No transactions today", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Log food or exercise to see transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryColor,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.transactions) { tx -> LedgerRow(tx) }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text("$value kcal", style = MaterialTheme.typography.titleLarge, color = color)
    }
}

@Composable
private fun LedgerRow(tx: CalorieTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
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
