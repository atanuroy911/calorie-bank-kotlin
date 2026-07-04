package com.roy.caloriebank.data.repository

import com.roy.caloriebank.data.local.CalorieTransactionDao
import com.roy.caloriebank.data.local.dateKeyOf
import com.roy.caloriebank.data.local.toDomain
import com.roy.caloriebank.data.local.toEntity
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.repository.TransactionRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl @Inject constructor(
    private val dao: CalorieTransactionDao,
) : TransactionRepository {
    override suspend fun addTransaction(transaction: CalorieTransaction) =
        dao.addTransaction(transaction.toEntity())

    override suspend fun getTransactionsForDate(userId: String, date: Instant): List<CalorieTransaction> =
        dao.getTransactionsForDate(userId, dateKeyOf(date)).map { it.toDomain() }

    override fun watchTransactionsForDate(userId: String, date: Instant): Flow<List<CalorieTransaction>> =
        dao.watchTransactionsForDate(userId, dateKeyOf(date)).map { list -> list.map { it.toDomain() } }

    override suspend fun getRecentTransactions(userId: String, limit: Int): List<CalorieTransaction> =
        dao.getRecentTransactions(userId, limit).map { it.toDomain() }
}
