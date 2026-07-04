package com.roy.caloriebank.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.UserProfile
import com.roy.caloriebank.domain.repository.UserProfileRepository
import com.roy.caloriebank.domain.util.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EditProfileState(
    val isLoading: Boolean = true,
    val name: String = "",
    val age: Int = 25,
    val gender: String = "male",
    val heightCm: Double = 170.0,
    val currentWeightKg: Double = 70.0,
    val goalWeightKg: Double = 65.0,
    val activityLevel: String = "moderate",
    val goal: String = "weight_loss",
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state

    private var existingProfile: UserProfile? = null

    init {
        viewModelScope.launch {
            val userId = preferencesRepository.userId.first()
            val profile = userId?.let { userProfileRepository.getProfile(it) }
            existingProfile = profile
            if (profile != null) {
                _state.value = EditProfileState(
                    isLoading = false,
                    name = profile.displayName,
                    age = profile.age,
                    gender = profile.gender,
                    heightCm = profile.heightCm,
                    currentWeightKg = profile.currentWeightKg,
                    goalWeightKg = profile.goalWeightKg,
                    activityLevel = profile.activityLevel,
                    goal = profile.goal,
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun updateName(v: String) { _state.value = _state.value.copy(name = v) }
    fun updateAge(v: Int) { _state.value = _state.value.copy(age = v.coerceIn(10, 100)) }
    fun updateGender(v: String) { _state.value = _state.value.copy(gender = v) }
    fun updateHeight(v: Double) { _state.value = _state.value.copy(heightCm = v) }
    fun updateCurrentWeight(v: Double) { _state.value = _state.value.copy(currentWeightKg = v) }
    fun updateGoalWeight(v: Double) { _state.value = _state.value.copy(goalWeightKg = v) }
    fun updateActivityLevel(v: String) { _state.value = _state.value.copy(activityLevel = v) }
    fun updateGoal(v: String) { _state.value = _state.value.copy(goal = v) }

    fun save(onSuccess: () -> Unit) {
        val current = existingProfile ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            try {
                val s = _state.value
                val goals = CalorieCalculator.calculateFromProfile(
                    weightKg = s.currentWeightKg,
                    heightCm = s.heightCm,
                    age = s.age,
                    gender = s.gender,
                    activityLevel = s.activityLevel,
                    goal = s.goal,
                )
                val updated = current.copy(
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
                    updatedAt = Instant.now(),
                )
                userProfileRepository.updateProfile(updated)
                _state.value = _state.value.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
