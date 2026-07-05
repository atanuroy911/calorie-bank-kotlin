package com.roy.caloriebank.data.repository

import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.data.remote.AiDataSource
import com.roy.caloriebank.data.remote.CustomBackendDataSource
import com.roy.caloriebank.data.remote.FirebaseAiDataSource
import com.roy.caloriebank.domain.model.AiResponse
import com.roy.caloriebank.domain.model.ChatMessage
import com.roy.caloriebank.domain.repository.AiRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Routes each chat turn to whichever backend the user has configured, falling back to Firebase AI
 * Logic (Gemini, gated by this app's own Firebase project — no user key needed) as the zero-config
 * default so CalBot works out of the box:
 *   1. Custom backend (BYO server), if the user turned it on and set a URL.
 *   2. Direct provider BYOK, if the user pasted in their own API key.
 *   3. Firebase AI Logic, otherwise.
 */
class AiRepositoryImpl @Inject constructor(
    private val aiDataSource: AiDataSource,
    private val customBackendDataSource: CustomBackendDataSource,
    private val firebaseAiDataSource: FirebaseAiDataSource,
    private val preferencesRepository: PreferencesRepository,
) : AiRepository {

    override suspend fun sendMessage(
        sessionId: String,
        history: List<ChatMessage>,
        userMessage: String,
        userContext: Map<String, Any?>?,
    ): AiResponse {
        val useCustomBackend = preferencesRepository.useCustomBackend.first()
        val backendUrl = preferencesRepository.backendUrl.first()
        val apiKey = preferencesRepository.aiApiKey.first()

        return when {
            useCustomBackend && backendUrl.isNotEmpty() -> {
                val token = preferencesRepository.backendToken.first()
                customBackendDataSource.sendMessage(
                    sessionId = sessionId,
                    history = history,
                    userMessage = userMessage,
                    userContext = userContext,
                    backendUrl = backendUrl,
                    token = token,
                )
            }
            !apiKey.isNullOrBlank() -> {
                val provider = preferencesRepository.aiProvider.first()
                aiDataSource.sendMessage(
                    provider = provider,
                    apiKey = apiKey,
                    history = history,
                    userMessage = userMessage,
                )
            }
            else -> firebaseAiDataSource.sendMessage(
                history = history,
                userMessage = userMessage,
                userContext = userContext,
            )
        }
    }
}
