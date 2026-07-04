package com.roy.caloriebank.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.Macros
import com.roy.caloriebank.domain.model.Micros
import java.time.Instant

@Entity(
    tableName = "daily_summaries",
    indices = [
        Index(value = ["userId", "dateKey"], name = "idx_summary_user_date"),
        Index(value = ["userId", "dateKey"], unique = true, name = "idx_summary_user_date_unique"),
    ],
)
@TypeConverters(Converters::class)
data class DailySummaryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val dateKey: String,
    val date: Instant,
    val budget: Int,
    val consumed: Int = 0,
    val exerciseBonus: Int = 0,
    val bankBonus: Int = 0,
    val macros: Macros = Macros(),
    val micros: Micros = Micros(),
    val endOfDayProcessed: Boolean = false,
)

fun DailySummaryEntity.toDomain(): DailySummary = DailySummary(
    userId = userId,
    date = date,
    budget = budget,
    consumed = consumed,
    exerciseBonus = exerciseBonus,
    bankBonus = bankBonus,
    macros = macros,
    micros = micros,
    endOfDayProcessed = endOfDayProcessed,
)

fun DailySummary.toEntity(id: String, dateKey: String): DailySummaryEntity = DailySummaryEntity(
    id = id,
    userId = userId,
    dateKey = dateKey,
    date = date,
    budget = budget,
    consumed = consumed,
    exerciseBonus = exerciseBonus,
    bankBonus = bankBonus,
    macros = macros,
    micros = micros,
    endOfDayProcessed = endOfDayProcessed,
)
