package com.roy.caloriebank.domain.model

import java.time.Instant

data class ExerciseEntry(
    val id: String,
    val timestamp: Instant,
    val exerciseName: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val notes: String? = null,
    val userId: String,
)
