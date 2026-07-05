package com.roy.caloriebank.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.BankAccount
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.UserProfile
import com.roy.caloriebank.domain.repository.BankRepository
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val bankAccount: BankAccount? = null,
    val summary: DailySummary? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val userProfileRepository: UserProfileRepository,
    private val bankRepository: BankRepository,
    private val dailySummaryRepository: DailySummaryRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = preferencesRepository.userId.flatMapLatest { userId ->
        if (userId == null) return@flatMapLatest emptyFlow<ProfileUiState>()
        val now = Instant.now()
        combine(
            userProfileRepository.watchProfile(userId),
            bankRepository.watchBankAccount(userId),
            dailySummaryRepository.watchSummaryForDate(userId, now),
            preferencesRepository.currentStreak,
            preferencesRepository.longestStreak,
        ) { profile, bank, summary, currentStreak, longestStreak ->
            ProfileUiState(
                profile = profile,
                bankAccount = bank,
                summary = summary,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.clear()
            onSignedOut()
        }
    }
}
