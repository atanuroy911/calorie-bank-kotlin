package com.roy.caloriebank.data.repository

import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.data.remote.AiDataSource
import com.roy.caloriebank.data.remote.CustomBackendDataSource
import com.roy.caloriebank.domain.model.AiResponse
import com.roy.caloriebank.domain.model.ChatMessage
import com.roy.caloriebank.domain.repository.AiRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AiRepositoryImpl @Inject constructor(
    private val aiDataSource: AiDataSource,
    private val customBackendDataSource: CustomBackendDataSource,
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
        return if (useCustomBackend && backendUrl.isNotEmpty()) {
            val token = preferencesRepository.backendToken.first()
            customBackendDataSource.sendMessage(
                sessionId = sessionId,
                history = history,
                userMessage = userMessage,
                userContext = userContext,
                backendUrl = backendUrl,
                token = token,
            )
        } else {
            val apiKey = preferencesRepository.aiApiKey.first() ?: ""
            val provider = preferencesRepository.aiProvider.first()
            aiDataSource.sendMessage(
                provider = provider,
                apiKey = apiKey,
                history = history,
                userMessage = userMessage,
            )
        }
    }
}
