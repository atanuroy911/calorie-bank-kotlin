package com.roy.caloriebank.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.BankAccount
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.FoodEntry
import com.roy.caloriebank.domain.model.UserProfile
import com.roy.caloriebank.domain.repository.BankRepository
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.FoodLogRepository
import com.roy.caloriebank.domain.repository.TransactionRepository
import com.roy.caloriebank.domain.repository.UserProfileRepository
import com.roy.caloriebank.domain.util.isSameDay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val userId: String? = null,
    val profile: UserProfile? = null,
    val summary: DailySummary? = null,
    val bankAccount: BankAccount? = null,
    val todaysFoodEntries: List<FoodEntry> = emptyList(),
    val todaysTransactions: List<CalorieTransaction> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val userProfileRepository: UserProfileRepository,
    private val dailySummaryRepository: DailySummaryRepository,
    private val bankRepository: BankRepository,
    private val foodLogRepository: FoodLogRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val userIdFlow = preferencesRepository.userId

    val uiState: StateFlow<HomeUiState> = userIdFlow.flatMapLatest { userId ->
        if (userId == null) {
            return@flatMapLatest emptyFlow<HomeUiState>()
        }
        val now = Instant.now()
        combine(
            userProfileRepository.watchProfile(userId),
            dailySummaryRepository.watchSummaryForDate(userId, now),
            bankRepository.watchBankAccount(userId),
            foodLogRepository.watchEntriesForDate(userId, now),
            transactionRepository.watchTransactionsForDate(userId, now),
        ) { profile, summary, bank, foods, txs ->
            HomeUiState(
                userId = userId,
                profile = profile,
                summary = summary,
                bankAccount = bank,
                todaysFoodEntries = foods,
                todaysTransactions = txs,
                isLoading = false,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private val _greeting = MutableStateFlow(greetingForNow())
    val greeting: StateFlow<String> = _greeting

    init {
        runEndOfDayCheck()
    }

    /** Call again e.g. on screen resume to re-run the end-of-day banking logic. */
    fun runEndOfDayCheck() {
        viewModelScope.launch {
            performEndOfDayCheck()
        }
    }

    private suspend fun performEndOfDayCheck() {
        val userId = preferencesRepository.userId.first() ?: return
        val today = Instant.now()
        val lastActiveDateStr = preferencesRepository.lastActiveDate.first()

        if (lastActiveDateStr != null) {
            val lastActive = runCatching { Instant.parse(lastActiveDateStr) }.getOrNull()
            val isSameDay = lastActive != null && lastActive.isSameDay(today)
            if (!isSameDay) {
                val yesterday = today.minusSeconds(24 * 3600)
                val yesterdaySummary = dailySummaryRepository.getSummaryForDate(userId, yesterday)
                if (yesterdaySummary != null && !yesterdaySummary.endOfDayProcessed) {
                    val remaining = yesterdaySummary.remaining
                    if (remaining > 0) {
                        val zoned = yesterday.atZone(ZoneId.systemDefault())
                        val month = zoned.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        bankRepository.deposit(userId, remaining, "Daily Savings — $month ${zoned.dayOfMonth}")
                    }
                    dailySummaryRepository.updateSummary(yesterdaySummary.copy(endOfDayProcessed = true))
                }
            }
        }

        val todaysSummary = dailySummaryRepository.getSummaryForDate(userId, today)
        if (todaysSummary == null) {
            val profile = userProfileRepository.getProfile(userId)
            val budget = profile?.dailyCalorieBudget ?: 2000
            dailySummaryRepository.saveSummary(DailySummary(userId = userId, date = today, budget = budget))
        }
        preferencesRepository.setLastActiveDate(today.toString())
    }
}

private fun greetingForNow(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning ☀️"
        hour < 17 -> "Good afternoon 🌤️"
        else -> "Good evening 🌙"
    }
}
