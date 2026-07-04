package com.roy.caloriebank.data.repository

import com.roy.caloriebank.data.local.DailySummaryDao
import com.roy.caloriebank.data.local.dateKeyOf
import com.roy.caloriebank.data.local.toDomain
import com.roy.caloriebank.data.local.toEntity
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DailySummaryRepositoryImpl @Inject constructor(
    private val dao: DailySummaryDao,
) : DailySummaryRepository {

    override suspend fun getSummaryForDate(userId: String, date: Instant): DailySummary? =
        dao.getSummaryForDate(userId, dateKeyOf(date))?.toDomain()

    override suspend fun saveSummary(summary: DailySummary) {
        dao.saveSummary(summary.toEntity(id = UUID.randomUUID().toString(), dateKey = dateKeyOf(summary.date)))
    }

    /** Updates only the mutable fields (consumed/exerciseBonus/bankBonus/macros/micros/endOfDayProcessed). */
    override suspend fun updateSummary(summary: DailySummary) {
        val dateKey = dateKeyOf(summary.date)
        val existing = dao.getSummaryForDate(summary.userId, dateKey)
        if (existing == null) {
            saveSummary(summary)
            return
        }
        dao.saveSummary(
            existing.copy(
                consumed = summary.consumed,
                exerciseBonus = summary.exerciseBonus,
                bankBonus = summary.bankBonus,
                macros = summary.macros,
                micros = summary.micros,
                endOfDayProcessed = summary.endOfDayProcessed,
            ),
        )
    }

    override fun watchSummaryForDate(userId: String, date: Instant): Flow<DailySummary?> =
        dao.watchSummaryForDate(userId, dateKeyOf(date)).map { it?.toDomain() }
}
