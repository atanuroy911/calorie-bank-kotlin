package com.roy.caloriebank.data.remote

import com.roy.caloriebank.domain.model.AiResponse
import com.roy.caloriebank.domain.model.ChatMessage
import javax.inject.Inject

interface AiDataSource {
    suspend fun sendMessage(
        provider: String,
        apiKey: String,
        history: List<ChatMessage>,
        userMessage: String,
        model: String? = null,
    ): AiResponse
}

class FakeAiDataSource @Inject constructor() : AiDataSource {
    override suspend fun sendMessage(
        provider: String,
        apiKey: String,
        history: List<ChatMessage>,
        userMessage: String,
        model: String?,
    ): AiResponse {
        kotlinx.coroutines.delay(500)
        return AiResponse(
            message = "AI features are not yet connected. This is a placeholder response.",
            action = "none",
            data = null,
        )
    }
}
