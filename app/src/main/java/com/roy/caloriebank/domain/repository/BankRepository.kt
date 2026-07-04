package com.roy.caloriebank.domain.repository

import com.roy.caloriebank.domain.model.BankAccount
import com.roy.caloriebank.domain.model.CalorieTransaction
import kotlinx.coroutines.flow.Flow

interface BankRepository {
    suspend fun getBankAccount(userId: String): BankAccount
    suspend fun updateBankBalance(userId: String, newBalance: Int)
    suspend fun deposit(userId: String, calories: Int, label: String)
    suspend fun withdraw(userId: String, calories: Int, label: String)
    fun watchBankAccount(userId: String): Flow<BankAccount>
    suspend fun getBankTransactions(userId: String, limit: Int = 30): List<CalorieTransaction>
}
