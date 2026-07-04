package com.roy.caloriebank.ui.bank

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.roy.caloriebank.domain.model.isPositive
import com.roy.caloriebank.domain.util.kcalFormatted
import com.roy.caloriebank.domain.util.signedKcal
import com.roy.caloriebank.domain.util.timeLabel
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.AccentColor
import com.roy.caloriebank.ui.theme.AccentGradient
import com.roy.caloriebank.ui.theme.AppTextStyles
import com.roy.caloriebank.ui.theme.BankBalanceCardGradient
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.PositiveColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor
import com.roy.caloriebank.ui.theme.WarningColor

@Composable
fun BankScreen(
    onWithdraw: () -> Unit,
    viewModel: BankViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text("Calorie Bank", style = MaterialTheme.typography.headlineLarge) }

        item {
            BankBalanceCard(
                balance = uiState.bankAccount?.balance ?: 0,
                onWithdraw = onWithdraw,
            )
        }

        item { Text("Recent Bank Activity", style = MaterialTheme.typography.titleLarge) }

        if (uiState.bankTransactionsToday.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Rounded.Savings, contentDescription = null, tint = TextSecondaryColor)
                    Spacer(Modifier.height(8.dp))
                    Text("No bank activity yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Save calories today and they'll appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            items(uiState.bankTransactionsToday) { tx -> BankTxRow(tx) }
        }

        item { HowItWorksCard() }
    }
}

@Composable
private fun BankBalanceCard(balance: Int, onWithdraw: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BankBalanceCardGradient)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = AccentColor)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("Savings Account", style = MaterialTheme.typography.titleSmall)
                Text("Calorie Bank", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("${balance.kcalFormatted()} kcal", style = AppTextStyles.bankBalance)
        Spacer(Modifier.height(20.dp))
        GradientButton(
            text = "Withdraw Calories",
            onClick = onWithdraw,
            enabled = balance > 0,
            gradient = AccentGradient,
        )
    }
}

@Composable
private fun BankTxRow(tx: CalorieTransaction) {
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
            Icon(
                if (tx.type.isPositive) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                contentDescription = null,
                tint = if (tx.type.isPositive) PositiveColor else NegativeColor,
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(tx.label, style = MaterialTheme.typography.titleMedium)
                Text(tx.timestamp.timeLabel(), style = MaterialTheme.typography.bodySmall, color = TextSecondaryColor)
            }
        }
        Text(
            tx.bankSignedCalories.signedKcal(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (tx.type.isPositive) PositiveColor else NegativeColor,
        )
    }
}

@Composable
private fun HowItWorksCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
            .padding(16.dp),
    ) {
        Text("How it works", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        HowItWorksStep("1", "Stay under budget → unused calories are auto-saved", PositiveColor)
        HowItWorksStep("2", "Savings accumulate in your bank balance", AccentColor)
        HowItWorksStep("3", "Withdraw when you want a cheat meal or special occasion", WarningColor)
    }
}

@Composable
private fun HowItWorksStep(number: String, text: String, color: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(number, style = MaterialTheme.typography.titleMedium, color = color)
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
    }
}
