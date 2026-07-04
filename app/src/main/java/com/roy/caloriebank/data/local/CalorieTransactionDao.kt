package com.roy.caloriebank.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CalorieTransactionDao {
    @Upsert
    suspend fun addTransaction(transaction: CalorieTransactionEntity)

    @Query("SELECT * FROM calorie_transactions WHERE userId = :userId AND dateKey = :dateKey ORDER BY timestamp DESC")
    suspend fun getTransactionsForDate(userId: String, dateKey: String): List<CalorieTransactionEntity>

    @Query("SELECT * FROM calorie_transactions WHERE userId = :userId AND dateKey = :dateKey ORDER BY timestamp DESC")
    fun watchTransactionsForDate(userId: String, dateKey: String): Flow<List<CalorieTransactionEntity>>

    @Query("SELECT * FROM calorie_transactions WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTransactions(userId: String, limit: Int = 20): List<CalorieTransactionEntity>

    @Query(
        "SELECT * FROM calorie_transactions WHERE userId = :userId AND (type = 'DailySavingsDeposit' OR type = 'BankWithdrawal') ORDER BY timestamp DESC LIMIT :limit",
    )
    suspend fun getBankTransactions(userId: String, limit: Int = 30): List<CalorieTransactionEntity>

    @Query(
        "SELECT * FROM calorie_transactions WHERE userId = :userId AND (type = 'DailySavingsDeposit' OR type = 'BankWithdrawal') ORDER BY timestamp DESC LIMIT :limit",
    )
    fun watchBankTransactions(userId: String, limit: Int = 30): Flow<List<CalorieTransactionEntity>>
}
