package com.roy.caloriebank.data.repository

import com.roy.caloriebank.data.local.FoodEntryDao
import com.roy.caloriebank.data.local.dateKeyOf
import com.roy.caloriebank.data.local.toDomain
import com.roy.caloriebank.data.local.toEntity
import com.roy.caloriebank.domain.model.FoodEntry
import com.roy.caloriebank.domain.repository.FoodLogRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FoodLogRepositoryImpl @Inject constructor(
    private val dao: FoodEntryDao,
) : FoodLogRepository {
    override suspend fun addFoodEntry(entry: FoodEntry) =
        dao.addFoodEntry(entry.toEntity(dateKeyOf(entry.timestamp)))

    override suspend fun deleteFoodEntry(entryId: String) = dao.deleteFoodEntry(entryId)

    override suspend fun getEntriesForDate(userId: String, date: Instant): List<FoodEntry> =
        dao.getEntriesForDate(userId, dateKeyOf(date)).map { it.toDomain() }

    override fun watchEntriesForDate(userId: String, date: Instant): Flow<List<FoodEntry>> =
        dao.watchEntriesForDate(userId, dateKeyOf(date)).map { list -> list.map { it.toDomain() } }
}
