package com.roy.caloriebank.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE userId = :userId AND dateKey = :dateKey LIMIT 1")
    suspend fun getSummaryForDate(userId: String, dateKey: String): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries WHERE userId = :userId AND dateKey = :dateKey LIMIT 1")
    fun watchSummaryForDate(userId: String, dateKey: String): Flow<DailySummaryEntity?>

    @Upsert
    suspend fun saveSummary(summary: DailySummaryEntity)
}
