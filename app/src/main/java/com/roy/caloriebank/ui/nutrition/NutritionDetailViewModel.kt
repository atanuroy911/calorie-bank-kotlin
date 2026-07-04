package com.roy.caloriebank.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.UserProfile
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class NutritionDetailUiState(
    val summary: DailySummary? = null,
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class NutritionDetailViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    dailySummaryRepository: DailySummaryRepository,
    userProfileRepository: UserProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<NutritionDetailUiState> = preferencesRepository.userId.flatMapLatest { userId ->
        if (userId == null) return@flatMapLatest emptyFlow<NutritionDetailUiState>()
        val now = Instant.now()
        combine(
            dailySummaryRepository.watchSummaryForDate(userId, now),
            userProfileRepository.watchProfile(userId),
        ) { summary, profile ->
            NutritionDetailUiState(summary = summary, profile = profile, isLoading = false)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionDetailUiState(),
    )
}
