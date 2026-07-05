package com.roy.caloriebank.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserProfileEntity::class,
        FoodEntryEntity::class,
        CalorieTransactionEntity::class,
        BankAccountEntity::class,
        DailySummaryEntity::class,
        FoodCatalogEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class CalorieBankDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun calorieTransactionDao(): CalorieTransactionDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun foodCatalogDao(): FoodCatalogDao
}
