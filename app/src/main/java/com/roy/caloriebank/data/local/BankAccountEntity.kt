package com.roy.caloriebank.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.roy.caloriebank.domain.model.BankAccount
import java.time.Instant

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey val userId: String,
    val balance: Int = 0,
    val lastUpdated: Instant,
)

fun BankAccountEntity.toDomain(): BankAccount = BankAccount(
    userId = userId,
    balance = balance,
    lastUpdated = lastUpdated,
)

fun BankAccount.toEntity(): BankAccountEntity = BankAccountEntity(
    userId = userId,
    balance = balance,
    lastUpdated = lastUpdated,
)
