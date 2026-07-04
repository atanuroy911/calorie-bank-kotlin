package com.roy.caloriebank.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.roy.caloriebank.domain.model.FoodEntry
import com.roy.caloriebank.domain.model.FoodItem
import com.roy.caloriebank.domain.model.Macros
import com.roy.caloriebank.domain.model.Micros
import java.time.Instant

@Entity(
    tableName = "food_entries",
    indices = [Index(value = ["userId", "dateKey"], name = "idx_food_user_date")],
)
@TypeConverters(Converters::class)
data class FoodEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val timestamp: Instant,
    val dateKey: String,
    val mealType: String,
    val foods: List<FoodItem>,
    val totalCalories: Int,
    val macros: Macros = Macros(),
    val micros: Micros = Micros(),
    val aiSessionId: String? = null,
)

fun FoodEntryEntity.toDomain(): FoodEntry = FoodEntry(
    id = id,
    timestamp = timestamp,
    mealType = mealType,
    foods = foods,
    totalCalories = totalCalories,
    totalMacros = macros,
    totalMicros = micros,
    aiSessionId = aiSessionId,
    userId = userId,
)

fun FoodEntry.toEntity(dateKey: String): FoodEntryEntity = FoodEntryEntity(
    id = id,
    userId = userId,
    timestamp = timestamp,
    dateKey = dateKey,
    mealType = mealType,
    foods = foods,
    totalCalories = totalCalories,
    macros = totalMacros,
    micros = totalMicros,
    aiSessionId = aiSessionId,
)
