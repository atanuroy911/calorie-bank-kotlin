package com.roy.caloriebank.domain.repository

import com.roy.caloriebank.domain.model.FoodEntry
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface FoodLogRepository {
    suspend fun addFoodEntry(entry: FoodEntry)
    suspend fun deleteFoodEntry(entryId: String)
    suspend fun getEntriesForDate(userId: String, date: Instant): List<FoodEntry>
    fun watchEntriesForDate(userId: String, date: Instant): Flow<List<FoodEntry>>
}
