package com.roy.caloriebank.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signInWithGoogle(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            delay(1000)
            preferencesRepository.setUserId("google_user_${System.currentTimeMillis()}")
            _uiState.value = AuthUiState(isLoading = false)
            onComplete()
        }
    }

    fun continueAsGuest(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            preferencesRepository.setUserId("guest_${System.currentTimeMillis()}")
            _uiState.value = AuthUiState(isLoading = false)
            onComplete()
        }
    }

    fun finishIntro(onComplete: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.setIntroSeen(true)
            onComplete()
        }
    }
}
