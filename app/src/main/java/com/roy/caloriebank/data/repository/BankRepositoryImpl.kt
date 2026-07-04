package com.roy.caloriebank.data.repository

import com.roy.caloriebank.data.local.BankAccountDao
import com.roy.caloriebank.data.local.CalorieTransactionDao
import com.roy.caloriebank.data.local.dateKeyOf
import com.roy.caloriebank.data.local.toDomain
import com.roy.caloriebank.data.local.toEntity
import com.roy.caloriebank.domain.model.BankAccount
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.TransactionType
import com.roy.caloriebank.domain.repository.BankRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BankRepositoryImpl @Inject constructor(
    private val bankDao: BankAccountDao,
    private val transactionDao: CalorieTransactionDao,
) : BankRepository {

    override suspend fun getBankAccount(userId: String): BankAccount {
        val existing = bankDao.getBankAccount(userId)
        if (existing != null) return existing.toDomain()
        val fresh = BankAccount(userId = userId, balance = 0, lastUpdated = Instant.now())
        bankDao.upsert(fresh.toEntity())
        return fresh
    }

    override suspend fun updateBankBalance(userId: String, newBalance: Int) {
        bankDao.upsert(
            BankAccount(userId = userId, balance = newBalance, lastUpdated = Instant.now()).toEntity(),
        )
    }

    override suspend fun deposit(userId: String, calories: Int, label: String) {
        val account = getBankAccount(userId)
        val newBalance = account.balance + calories
        updateBankBalance(userId, newBalance)
        val now = Instant.now()
        transactionDao.addTransaction(
            CalorieTransaction(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                type = TransactionType.DailySavingsDeposit,
                calories = calories,
                label = label,
                userId = userId,
                date = dateKeyOf(now),
            ).toEntity(),
        )
    }

    override suspend fun withdraw(userId: String, calories: Int, label: String) {
        val account = getBankAccount(userId)
        val newBalance = (account.balance - calories).coerceIn(0, account.balance)
        updateBankBalance(userId, newBalance)
        val now = Instant.now()
        transactionDao.addTransaction(
            CalorieTransaction(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                type = TransactionType.BankWithdrawal,
                calories = calories,
                label = label,
                userId = userId,
                date = dateKeyOf(now),
            ).toEntity(),
        )
    }

    override fun watchBankAccount(userId: String): Flow<BankAccount> =
        bankDao.watchBankAccount(userId).map { it?.toDomain() ?: BankAccount(userId, 0, Instant.now()) }

    override suspend fun getBankTransactions(userId: String, limit: Int): List<CalorieTransaction> =
        transactionDao.getBankTransactions(userId, limit).map { it.toDomain() }
}
