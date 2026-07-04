package com.roy.caloriebank.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Upsert
    suspend fun addFoodEntry(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :entryId")
    suspend fun deleteFoodEntry(entryId: String)

    @Query("SELECT * FROM food_entries WHERE userId = :userId AND dateKey = :dateKey ORDER BY timestamp ASC")
    suspend fun getEntriesForDate(userId: String, dateKey: String): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE userId = :userId AND dateKey = :dateKey ORDER BY timestamp ASC")
    fun watchEntriesForDate(userId: String, dateKey: String): Flow<List<FoodEntryEntity>>
}
