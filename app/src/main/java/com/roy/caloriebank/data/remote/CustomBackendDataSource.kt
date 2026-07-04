package com.roy.caloriebank.data.remote

import com.roy.caloriebank.domain.model.AiResponse
import com.roy.caloriebank.domain.model.ChatMessage
import javax.inject.Inject

data class BackendHealthResult(
    val isOk: Boolean,
    val message: String,
    val latencyMs: Long? = null,
)

interface CustomBackendDataSource {
    suspend fun sendMessage(
        sessionId: String,
        history: List<ChatMessage>,
        userMessage: String,
        userContext: Map<String, Any?>? = null,
        backendUrl: String,
        token: String?,
    ): AiResponse

    suspend fun testConnection(backendUrl: String): BackendHealthResult
}

class FakeCustomBackendDataSource @Inject constructor() : CustomBackendDataSource {
    override suspend fun sendMessage(
        sessionId: String,
        history: List<ChatMessage>,
        userMessage: String,
        userContext: Map<String, Any?>?,
        backendUrl: String,
        token: String?,
    ): AiResponse {
        kotlinx.coroutines.delay(500)
        return AiResponse(
            message = "AI features are not yet connected. This is a placeholder response.",
            action = "none",
            data = null,
        )
    }

    override suspend fun testConnection(backendUrl: String): BackendHealthResult {
        kotlinx.coroutines.delay(500)
        return BackendHealthResult(isOk = false, message = "Custom backend not yet implemented.")
    }
}
