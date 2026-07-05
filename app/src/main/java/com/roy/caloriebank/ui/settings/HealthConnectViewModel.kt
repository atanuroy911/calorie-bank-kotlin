package com.roy.caloriebank.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.health.HealthConnectRepository
import com.roy.caloriebank.data.local.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HealthUiState(
    val isAvailable: Boolean = false,
    val isLinked: Boolean = false,
    val steps: Long = 0,
    val activeCalories: Int = 0,
)

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val healthConnectRepository: HealthConnectRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val permissions: Set<String> = healthConnectRepository.permissions

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val available = healthConnectRepository.isAvailable(context)
            val linked = preferencesRepository.healthConnectLinked.first()
            _uiState.update { it.copy(isAvailable = available, isLinked = linked) }
            if (available && linked) refresh()
        }
    }

    /** Called when there's nothing to launch a permission request for (already linked): re-syncs. */
    fun requestSync() {
        if (!_uiState.value.isAvailable || !_uiState.value.isLinked) return
        viewModelScope.launch { refresh() }
    }

    fun onPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            val hasAll = granted.containsAll(permissions)
            preferencesRepository.setHealthConnectLinked(hasAll)
            _uiState.update { it.copy(isLinked = hasAll) }
            if (hasAll) refresh()
        }
    }

    private suspend fun refresh() {
        val client = healthConnectRepository.getClient(context)
        val summary = healthConnectRepository.readTodaySummary(client)
        _uiState.update { it.copy(steps = summary.steps, activeCalories = summary.activeCaloriesBurned.roundToInt()) }
    }
}
