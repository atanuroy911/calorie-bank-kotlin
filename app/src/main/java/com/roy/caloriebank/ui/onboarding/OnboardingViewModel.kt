package com.roy.caloriebank.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.UserProfile
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.UserProfileRepository
import com.roy.caloriebank.domain.util.CalorieCalculator
import com.roy.caloriebank.domain.util.NutritionGoals
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class OnboardingState(
    val currentPage: Int = 0,
    val name: String = "",
    val email: String = "",
    val age: Int = 25,
    val gender: String = "male",
    val unitSystem: String = "metric",
    val heightCm: Double = 170.0,
    val currentWeightKg: Double = 70.0,
    val goalWeightKg: Double = 65.0,
    val activityLevel: String = "moderate",
    val goal: String = "weight_loss",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val calculatedGoals: NutritionGoals
        get() = CalorieCalculator.calculateFromProfile(
            weightKg = currentWeightKg,
            heightCm = heightCm,
            age = age,
            gender = gender,
            activityLevel = activityLevel,
            goal = goal,
        )
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val userProfileRepository: UserProfileRepository,
    private val dailySummaryRepository: DailySummaryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun nextPage() {
        _state.value = _state.value.copy(currentPage = (_state.value.currentPage + 1).coerceAtMost(3))
    }

    fun prevPage() {
        _state.value = _state.value.copy(currentPage = (_state.value.currentPage - 1).coerceAtLeast(0))
    }

    fun updateName(v: String) { _state.value = _state.value.copy(name = v) }
    fun updateAge(v: Int) { _state.value = _state.value.copy(age = v.coerceIn(10, 100)) }
    fun updateGender(v: String) { _state.value = _state.value.copy(gender = v) }
    fun updateUnitSystem(v: String) { _state.value = _state.value.copy(unitSystem = v) }
    fun updateHeight(v: Double) { _state.value = _state.value.copy(heightCm = v) }
    fun updateCurrentWeight(v: Double) { _state.value = _state.value.copy(currentWeightKg = v) }
    fun updateGoalWeight(v: Double) { _state.value = _state.value.copy(goalWeightKg = v) }
    fun updateActivityLevel(v: String) { _state.value = _state.value.copy(activityLevel = v) }
    fun updateGoal(v: String) { _state.value = _state.value.copy(goal = v) }

    fun submit(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            try {
                val existingUserId = preferencesRepository.userId.first()
                val userId = existingUserId ?: UUID.randomUUID().toString()
                if (existingUserId == null) preferencesRepository.setUserId(userId)

                val s = _state.value
                val goals = s.calculatedGoals
                val now = Instant.now()
                val profile = UserProfile(
                    id = userId,
                    email = s.email,
                    displayName = s.name,
                    age = s.age,
                    gender = s.gender,
                    heightCm = s.heightCm,
                    currentWeightKg = s.currentWeightKg,
                    goalWeightKg = s.goalWeightKg,
                    activityLevel = s.activityLevel,
                    goal = s.goal,
                    dailyCalorieBudget = goals.dailyCalories,
                    dailyProteinGoalG = goals.proteinG,
                    dailyCarbsGoalG = goals.carbsG,
                    dailyFatGoalG = goals.fatG,
                    createdAt = now,
                    updatedAt = now,
                )
                userProfileRepository.saveProfile(profile)
                dailySummaryRepository.saveSummary(
                    DailySummary(userId = userId, date = now, budget = goals.dailyCalories),
                )
                preferencesRepository.setOnboardingCompleted(true)
                preferencesRepository.setLastActiveDate(now.toString())
                _state.value = _state.value.copy(isSubmitting = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSubmitting = false, error = e.message)
                onError(e.message ?: "Something went wrong")
            }
        }
    }
}
