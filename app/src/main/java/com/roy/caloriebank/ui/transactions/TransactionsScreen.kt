package com.roy.caloriebank.ui.transactions

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
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
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextMutedColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val DAYS_BACK = 60

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val days = remember(today) { (DAYS_BACK downTo 0).map { today.minusDays(it.toLong()) } }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(days.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(days) { day ->
                DayPill(
                    day = day,
                    isSelected = day == uiState.selectedDate,
                    isToday = day == today,
                    onClick = { viewModel.selectDate(day) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        val dateLabel = if (uiState.selectedDate == today) {
            "Today"
        } else {
            uiState.selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d"))
        }
        Text(
            dateLabel,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondaryColor,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard("Earned", uiState.earned, PositiveColor, Modifier.weight(1f))
            SummaryCard("Spent", uiState.spent, NegativeColor, Modifier.weight(1f))
            uiState.summary?.let { summary ->
                SummaryCard("Remaining", summary.remaining, PrimaryColor, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(8.dp))

        if (uiState.transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = TextMutedColor, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (uiState.selectedDate == today) "No transactions today" else "No transactions on this day",
                        style = MaterialTheme.typography.titleMedium,
                    )
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
private fun DayPill(day: LocalDate, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryColor else SurfaceColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) TextOnPrimaryColor else TextSecondaryColor,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = if (isToday && !isSelected) {
                Modifier.clip(CircleShape).background(SurfaceColor)
            } else {
                Modifier
            },
        ) {
            Text(
                "${day.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) TextOnPrimaryColor else if (isToday) PrimaryColor else TextSecondaryColor,
                textAlign = TextAlign.Center,
            )
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
