package com.roy.caloriebank.di

import com.roy.caloriebank.data.remote.AiDataSource
import com.roy.caloriebank.data.remote.CustomBackendDataSource
import com.roy.caloriebank.data.remote.FakeAiDataSource
import com.roy.caloriebank.data.remote.FakeCustomBackendDataSource
import com.roy.caloriebank.data.repository.AiRepositoryImpl
import com.roy.caloriebank.data.repository.BankRepositoryImpl
import com.roy.caloriebank.data.repository.DailySummaryRepositoryImpl
import com.roy.caloriebank.data.repository.FoodLogRepositoryImpl
import com.roy.caloriebank.data.repository.TransactionRepositoryImpl
import com.roy.caloriebank.data.repository.UserProfileRepositoryImpl
import com.roy.caloriebank.domain.repository.AiRepository
import com.roy.caloriebank.domain.repository.BankRepository
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.FoodLogRepository
import com.roy.caloriebank.domain.repository.TransactionRepository
import com.roy.caloriebank.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindFoodLogRepository(impl: FoodLogRepositoryImpl): FoodLogRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBankRepository(impl: BankRepositoryImpl): BankRepository

    @Binds
    @Singleton
    abstract fun bindDailySummaryRepository(impl: DailySummaryRepositoryImpl): DailySummaryRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    @Binds
    @Singleton
    abstract fun bindAiDataSource(impl: FakeAiDataSource): AiDataSource

    @Binds
    @Singleton
    abstract fun bindCustomBackendDataSource(impl: FakeCustomBackendDataSource): CustomBackendDataSource
}
