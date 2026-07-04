package com.roy.caloriebank.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BankAccountDao {
    @Query("SELECT * FROM bank_accounts WHERE userId = :userId LIMIT 1")
    suspend fun getBankAccount(userId: String): BankAccountEntity?

    @Query("SELECT * FROM bank_accounts WHERE userId = :userId LIMIT 1")
    fun watchBankAccount(userId: String): Flow<BankAccountEntity?>

    @Upsert
    suspend fun upsert(account: BankAccountEntity)
}
