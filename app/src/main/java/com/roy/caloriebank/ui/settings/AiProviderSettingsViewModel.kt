package com.roy.caloriebank.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.data.remote.CustomBackendDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AiProviderSettingsState(
    val useCustomBackend: Boolean = false,
    val backendUrl: String = "",
    val backendToken: String = "",
    val provider: String = "gemini",
    val apiKey: String = "",
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

val supportedAiProviders = listOf("gemini", "openai", "claude", "openrouter")

@HiltViewModel
class AiProviderSettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val customBackendDataSource: CustomBackendDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(AiProviderSettingsState())
    val state: StateFlow<AiProviderSettingsState> = _state

    init {
        viewModelScope.launch {
            _state.value = AiProviderSettingsState(
                useCustomBackend = preferencesRepository.useCustomBackend.first(),
                backendUrl = preferencesRepository.backendUrl.first(),
                backendToken = preferencesRepository.backendToken.first() ?: "",
                provider = preferencesRepository.aiProvider.first(),
                apiKey = preferencesRepository.aiApiKey.first() ?: "",
            )
        }
    }

    fun setUseCustomBackend(v: Boolean) { _state.value = _state.value.copy(useCustomBackend = v) }
    fun setBackendUrl(v: String) { _state.value = _state.value.copy(backendUrl = v) }
    fun setBackendToken(v: String) { _state.value = _state.value.copy(backendToken = v) }
    fun setProvider(v: String) { _state.value = _state.value.copy(provider = v) }
    fun setApiKey(v: String) { _state.value = _state.value.copy(apiKey = v) }

    fun testConnection() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isTesting = true, testResult = null)
            val result = customBackendDataSource.testConnection(_state.value.backendUrl)
            _state.value = _state.value.copy(
                isTesting = false,
                testResult = if (result.isOk) "Connected ✓" else "Failed: ${result.message}",
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, saved = false)
            val s = _state.value
            preferencesRepository.setUseCustomBackend(s.useCustomBackend)
            preferencesRepository.setBackendUrl(s.backendUrl)
            if (s.backendToken.isNotEmpty()) preferencesRepository.setBackendToken(s.backendToken)
            preferencesRepository.setAiProvider(s.provider)
            if (s.apiKey.isNotEmpty()) preferencesRepository.setAiApiKey(s.apiKey)
            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }
}
