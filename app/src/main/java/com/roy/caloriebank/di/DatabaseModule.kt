package com.roy.caloriebank.di

import android.content.Context
import androidx.room.Room
import com.roy.caloriebank.data.local.BankAccountDao
import com.roy.caloriebank.data.local.CalorieBankDatabase
import com.roy.caloriebank.data.local.CalorieTransactionDao
import com.roy.caloriebank.data.local.DailySummaryDao
import com.roy.caloriebank.data.local.FoodEntryDao
import com.roy.caloriebank.data.local.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CalorieBankDatabase =
        Room.databaseBuilder(context, CalorieBankDatabase::class.java, "calorie_bank.db").build()

    @Provides
    fun provideUserProfileDao(db: CalorieBankDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideFoodEntryDao(db: CalorieBankDatabase): FoodEntryDao = db.foodEntryDao()

    @Provides
    fun provideCalorieTransactionDao(db: CalorieBankDatabase): CalorieTransactionDao = db.calorieTransactionDao()

    @Provides
    fun provideBankAccountDao(db: CalorieBankDatabase): BankAccountDao = db.bankAccountDao()

    @Provides
    fun provideDailySummaryDao(db: CalorieBankDatabase): DailySummaryDao = db.dailySummaryDao()
}
