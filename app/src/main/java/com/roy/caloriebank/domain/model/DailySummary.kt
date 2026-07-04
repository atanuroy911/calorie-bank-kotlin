package com.roy.caloriebank.domain.model

import java.time.Instant

data class DailySummary(
    val userId: String,
    val date: Instant,
    val budget: Int,
    val consumed: Int = 0,
    val exerciseBonus: Int = 0,
    val bankBonus: Int = 0,
    val macros: Macros = Macros(),
    val micros: Micros = Micros(),
    val endOfDayProcessed: Boolean = false,
) {
    val proteinConsumedG: Double get() = macros.proteinG
    val carbsConsumedG: Double get() = macros.carbsG
    val fatConsumedG: Double get() = macros.fatG
    val fiberConsumedG: Double get() = macros.fiberG

    val totalAvailable: Int get() = budget + exerciseBonus + bankBonus
    val remaining: Int get() = (totalAvailable - consumed).coerceIn(0, totalAvailable.coerceAtLeast(0))
    val deficit: Int get() = (consumed - totalAvailable).coerceIn(0, consumed.coerceAtLeast(0))
    val isOverBudget: Boolean get() = consumed > totalAvailable
    val consumedPercent: Double get() = if (totalAvailable > 0) consumed.toDouble() / totalAvailable else 0.0
}
