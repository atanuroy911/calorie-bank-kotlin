package com.roy.caloriebank.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// NOTE: consider hardening ai_api_key/backend_token later via EncryptedSharedPreferences/Tink.
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val INTRO_SEEN = booleanPreferencesKey("intro_seen")
        val USER_ID = stringPreferencesKey("user_id")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val BACKEND_URL = stringPreferencesKey("backend_url")
        val BACKEND_TOKEN = stringPreferencesKey("backend_token")
        val USE_CUSTOM_BACKEND = booleanPreferencesKey("use_custom_backend")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val AI_USAGE_TODAY = intPreferencesKey("ai_usage_today")
        val AI_USAGE_DATE = stringPreferencesKey("ai_usage_date")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val notificationsEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    suspend fun setNotificationsEnabled(value: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = value }
    }

    val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }
    suspend fun setOnboardingCompleted(value: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = value }
    }

    val introSeen: Flow<Boolean> = dataStore.data.map { it[Keys.INTRO_SEEN] ?: false }
    suspend fun setIntroSeen(value: Boolean) {
        dataStore.edit { it[Keys.INTRO_SEEN] = value }
    }

    val userId: Flow<String?> = dataStore.data.map { it[Keys.USER_ID] }
    suspend fun setUserId(value: String) {
        dataStore.edit { it[Keys.USER_ID] = value }
    }

    val aiApiKey: Flow<String?> = dataStore.data.map { it[Keys.AI_API_KEY] }
    suspend fun setAiApiKey(value: String) {
        dataStore.edit { it[Keys.AI_API_KEY] = value }
    }
    suspend fun clearAiApiKey() {
        dataStore.edit { it.remove(Keys.AI_API_KEY) }
    }

    val aiProvider: Flow<String> = dataStore.data.map { it[Keys.AI_PROVIDER] ?: "gemini" }
    suspend fun setAiProvider(value: String) {
        dataStore.edit { it[Keys.AI_PROVIDER] = value }
    }

    val backendUrl: Flow<String> = dataStore.data.map { it[Keys.BACKEND_URL] ?: "" }
    suspend fun setBackendUrl(value: String) {
        dataStore.edit { it[Keys.BACKEND_URL] = value.trim() }
    }

    val backendToken: Flow<String?> = dataStore.data.map { it[Keys.BACKEND_TOKEN] }
    suspend fun setBackendToken(value: String) {
        dataStore.edit { it[Keys.BACKEND_TOKEN] = value }
    }
    suspend fun clearBackendToken() {
        dataStore.edit { it.remove(Keys.BACKEND_TOKEN) }
    }

    val useCustomBackend: Flow<Boolean> = dataStore.data.map { it[Keys.USE_CUSTOM_BACKEND] ?: false }
    suspend fun setUseCustomBackend(value: Boolean) {
        dataStore.edit { it[Keys.USE_CUSTOM_BACKEND] = value }
    }

    val lastActiveDate: Flow<String?> = dataStore.data.map { it[Keys.LAST_ACTIVE_DATE] }
    suspend fun setLastActiveDate(value: String) {
        dataStore.edit { it[Keys.LAST_ACTIVE_DATE] = value }
    }

    val aiUsageToday: Flow<Int> = dataStore.data.map { it[Keys.AI_USAGE_TODAY] ?: 0 }
    val aiUsageDate: Flow<String?> = dataStore.data.map { it[Keys.AI_USAGE_DATE] }

    suspend fun incrementAiUsage() {
        val today = LocalDate.now().toString()
        val prefs = dataStore.data.first()
        val storedDate = prefs[Keys.AI_USAGE_DATE]
        val newCount = if (storedDate != today) 1 else (prefs[Keys.AI_USAGE_TODAY] ?: 0) + 1
        dataStore.edit {
            it[Keys.AI_USAGE_DATE] = today
            it[Keys.AI_USAGE_TODAY] = newCount
        }
    }

    suspend fun resetAiUsage() {
        dataStore.edit {
            it[Keys.AI_USAGE_TODAY] = 0
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
