package com.roy.caloriebank.domain.model

import java.time.Instant

data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val age: Int,
    val gender: String,
    val heightCm: Double,
    val currentWeightKg: Double,
    val goalWeightKg: Double,
    val activityLevel: String,
    val goal: String,
    val dailyCalorieBudget: Int,
    val dailyProteinGoalG: Double,
    val dailyCarbsGoalG: Double,
    val dailyFatGoalG: Double,
    val dailyFiberGoalG: Double = 28.0,
    val dailySugarGoalG: Double = 50.0,
    val isPremium: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
