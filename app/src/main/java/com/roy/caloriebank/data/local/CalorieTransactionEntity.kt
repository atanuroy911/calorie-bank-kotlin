package com.roy.caloriebank.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.TransactionType
import java.time.Instant

@Entity(
    tableName = "calorie_transactions",
    indices = [Index(value = ["userId", "dateKey"], name = "idx_tx_user_date")],
)
data class CalorieTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val timestamp: Instant,
    val dateKey: String,
    val type: String,
    val calories: Int,
    val label: String,
    val foodEntryId: String? = null,
    val exerciseEntryId: String? = null,
)

fun CalorieTransactionEntity.toDomain(): CalorieTransaction = CalorieTransaction(
    id = id,
    timestamp = timestamp,
    type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.ManualAdjustment),
    calories = calories,
    label = label,
    foodEntryId = foodEntryId,
    exerciseEntryId = exerciseEntryId,
    userId = userId,
    date = dateKey,
)

fun CalorieTransaction.toEntity(): CalorieTransactionEntity = CalorieTransactionEntity(
    id = id,
    userId = userId,
    timestamp = timestamp,
    dateKey = date,
    type = type.name,
    calories = calories,
    label = label,
    foodEntryId = foodEntryId,
    exerciseEntryId = exerciseEntryId,
)
