package com.roy.caloriebank.domain.repository

import com.roy.caloriebank.domain.model.CalorieTransaction
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun addTransaction(transaction: CalorieTransaction)
    suspend fun getTransactionsForDate(userId: String, date: Instant): List<CalorieTransaction>
    fun watchTransactionsForDate(userId: String, date: Instant): Flow<List<CalorieTransaction>>
    suspend fun getRecentTransactions(userId: String, limit: Int = 20): List<CalorieTransaction>
}
