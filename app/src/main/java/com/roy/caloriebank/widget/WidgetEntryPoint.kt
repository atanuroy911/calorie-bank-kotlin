package com.roy.caloriebank.widget

import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.repository.BankRepository
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.UserProfileRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * GlanceAppWidget classes are instantiated by the widget host process, not by Hilt, so they
 * can't use @Inject directly — this lets them pull the same repository singletons Hilt already
 * built for the rest of the app via EntryPointAccessors.fromApplication(context, ...).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun preferencesRepository(): PreferencesRepository
    fun dailySummaryRepository(): DailySummaryRepository
    fun bankRepository(): BankRepository
    fun userProfileRepository(): UserProfileRepository
}
