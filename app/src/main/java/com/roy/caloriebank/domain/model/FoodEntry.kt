package com.roy.caloriebank.domain.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class FoodItem(
    val name: String,
    val quantity: String,
    val calories: Int,
    val macros: Macros = Macros(),
    val micros: Micros = Micros(),
) {
    val proteinG: Double get() = macros.proteinG
    val carbsG: Double get() = macros.carbsG
    val fatG: Double get() = macros.fatG
    val fiberG: Double get() = macros.fiberG
    val sugarG: Double get() = macros.sugarG
    val saturatedFatG: Double get() = macros.saturatedFatG
    val cholesterolMg: Double get() = macros.cholesterolMg
    val sodiumMg: Double get() = micros.sodiumMg
}

data class FoodEntry(
    val id: String,
    val timestamp: Instant,
    val mealType: String,
    val foods: List<FoodItem>,
    val totalCalories: Int,
    val totalMacros: Macros = Macros(),
    val totalMicros: Micros = Micros(),
    val aiSessionId: String? = null,
    val userId: String,
) {
    val totalProteinG: Double get() = totalMacros.proteinG
    val totalCarbsG: Double get() = totalMacros.carbsG
    val totalFatG: Double get() = totalMacros.fatG
    val totalFiberG: Double get() = totalMacros.fiberG

    val mealTypeLabel: String
        get() = when (mealType) {
            "breakfast" -> "Breakfast"
            "lunch" -> "Lunch"
            "dinner" -> "Dinner"
            "snack" -> "Snack"
            else -> mealType
        }

    companion object {
        fun fromFoods(
            id: String,
            mealType: String,
            foods: List<FoodItem>,
            aiSessionId: String? = null,
            userId: String,
        ): FoodEntry {
            val totalMacros = foods.fold(Macros()) { acc, f -> acc + f.macros }
            val totalMicros = foods.fold(Micros()) { acc, f -> acc + f.micros }
            val totalCalories = foods.fold(0) { sum, f -> sum + f.calories }
            return FoodEntry(
                id = id,
                timestamp = Instant.now(),
                mealType = mealType,
                foods = foods,
                totalCalories = totalCalories,
                totalMacros = totalMacros,
                totalMicros = totalMicros,
                aiSessionId = aiSessionId,
                userId = userId,
            )
        }
    }
}
