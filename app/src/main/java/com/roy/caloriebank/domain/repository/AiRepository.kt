package com.roy.caloriebank.domain.repository

import com.roy.caloriebank.domain.model.AiResponse
import com.roy.caloriebank.domain.model.ChatMessage

interface AiRepository {
    suspend fun sendMessage(
        sessionId: String,
        history: List<ChatMessage>,
        userMessage: String,
        userContext: Map<String, Any?>? = null,
    ): AiResponse
}
