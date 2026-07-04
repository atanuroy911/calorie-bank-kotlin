package com.roy.caloriebank.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.roy.caloriebank.domain.model.UserProfile
import java.time.Instant

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
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

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    email = email,
    displayName = displayName,
    age = age,
    gender = gender,
    heightCm = heightCm,
    currentWeightKg = currentWeightKg,
    goalWeightKg = goalWeightKg,
    activityLevel = activityLevel,
    goal = goal,
    dailyCalorieBudget = dailyCalorieBudget,
    dailyProteinGoalG = dailyProteinGoalG,
    dailyCarbsGoalG = dailyCarbsGoalG,
    dailyFatGoalG = dailyFatGoalG,
    dailyFiberGoalG = dailyFiberGoalG,
    dailySugarGoalG = dailySugarGoalG,
    isPremium = isPremium,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id,
    email = email,
    displayName = displayName,
    age = age,
    gender = gender,
    heightCm = heightCm,
    currentWeightKg = currentWeightKg,
    goalWeightKg = goalWeightKg,
    activityLevel = activityLevel,
    goal = goal,
    dailyCalorieBudget = dailyCalorieBudget,
    dailyProteinGoalG = dailyProteinGoalG,
    dailyCarbsGoalG = dailyCarbsGoalG,
    dailyFatGoalG = dailyFatGoalG,
    dailyFiberGoalG = dailyFiberGoalG,
    dailySugarGoalG = dailySugarGoalG,
    isPremium = isPremium,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
