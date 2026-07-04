package com.roy.caloriebank.domain.repository

import com.roy.caloriebank.domain.model.DailySummary
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface DailySummaryRepository {
    suspend fun getSummaryForDate(userId: String, date: Instant): DailySummary?
    suspend fun saveSummary(summary: DailySummary)
    suspend fun updateSummary(summary: DailySummary)
    fun watchSummaryForDate(userId: String, date: Instant): Flow<DailySummary?>
}
