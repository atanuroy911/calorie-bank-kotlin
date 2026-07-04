package com.roy.caloriebank.domain.util

import kotlin.math.roundToInt

data class NutritionGoals(
    val dailyCalories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
)

object CalorieCalculator {

    private const val SEDENTARY_MULTIPLIER = 1.2
    private const val LIGHT_MULTIPLIER = 1.375
    private const val MODERATE_MULTIPLIER = 1.55
    private const val ACTIVE_MULTIPLIER = 1.725
    private const val VERY_ACTIVE_MULTIPLIER = 1.9

    private const val WEIGHT_LOSS_ADJUSTMENT = -500
    private const val MAINTENANCE_ADJUSTMENT = 0
    private const val WEIGHT_GAIN_ADJUSTMENT = 300

    private const val PROTEIN_PERCENT = 0.30
    private const val CARBS_PERCENT = 0.40
    private const val FAT_PERCENT = 0.30

    private const val PROTEIN_KCAL_PER_GRAM = 4.0
    private const val CARBS_KCAL_PER_GRAM = 4.0
    private const val FAT_KCAL_PER_GRAM = 9.0

    private const val MIN_BUDGET = 1200
    private const val MAX_BUDGET = 5000

    fun calculateBMR(weightKg: Double, heightCm: Double, age: Int, gender: String): Double {
        val base = (10 * weightKg) + (6.25 * heightCm) - (5 * age)
        return if (gender.lowercase() == "male") base + 5 else base - 161
    }

    fun activityMultiplier(activityLevel: String): Double = when (activityLevel.lowercase()) {
        "sedentary" -> SEDENTARY_MULTIPLIER
        "light" -> LIGHT_MULTIPLIER
        "moderate" -> MODERATE_MULTIPLIER
        "active" -> ACTIVE_MULTIPLIER
        "very_active" -> VERY_ACTIVE_MULTIPLIER
        else -> MODERATE_MULTIPLIER
    }

    fun calculateTDEE(bmr: Double, activityLevel: String): Double = bmr * activityMultiplier(activityLevel)

    fun goalAdjustment(goal: String): Int = when (goal.lowercase()) {
        "weight_loss" -> WEIGHT_LOSS_ADJUSTMENT
        "maintenance" -> MAINTENANCE_ADJUSTMENT
        "weight_gain" -> WEIGHT_GAIN_ADJUSTMENT
        else -> MAINTENANCE_ADJUSTMENT
    }

    fun calculateDailyBudget(tdee: Double, goal: String): Int {
        val adjustment = goalAdjustment(goal)
        return (tdee + adjustment).roundToInt().coerceIn(MIN_BUDGET, MAX_BUDGET)
    }

    fun calculateProteinGoal(dailyCalories: Int): Double =
        (dailyCalories * PROTEIN_PERCENT) / PROTEIN_KCAL_PER_GRAM

    fun calculateCarbsGoal(dailyCalories: Int): Double =
        (dailyCalories * CARBS_PERCENT) / CARBS_KCAL_PER_GRAM

    fun calculateFatGoal(dailyCalories: Int): Double =
        (dailyCalories * FAT_PERCENT) / FAT_KCAL_PER_GRAM

    fun calculateFromProfile(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String,
    ): NutritionGoals {
        val bmr = calculateBMR(weightKg, heightCm, age, gender)
        val tdee = calculateTDEE(bmr, activityLevel)
        val dailyCalories = calculateDailyBudget(tdee, goal)
        return NutritionGoals(
            dailyCalories = dailyCalories,
            proteinG = calculateProteinGoal(dailyCalories),
            carbsG = calculateCarbsGoal(dailyCalories),
            fatG = calculateFatGoal(dailyCalories),
        )
    }
}
