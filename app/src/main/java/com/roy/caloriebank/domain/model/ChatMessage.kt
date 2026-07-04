package com.roy.caloriebank.domain.model

import java.time.Instant

enum class ChatRole { User, Assistant, System }

enum class ChatMessageStatus { Sending, Sent, Error }

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: ChatRole,
    val content: String,
    val timestamp: Instant,
    val status: ChatMessageStatus = ChatMessageStatus.Sent,
    val hasFoodLog: Boolean = false,
    val hasExerciseLog: Boolean = false,
) {
    val isUser: Boolean get() = role == ChatRole.User
    val isAssistant: Boolean get() = role == ChatRole.Assistant
}

data class AiResponse(
    val message: String,
    val action: String,
    val data: Map<String, Any?>? = null,
) {
    val isFoodLog: Boolean get() = action == "food_log"
    val isExerciseLog: Boolean get() = action == "exercise_log"
    val isBankWithdraw: Boolean get() = action == "bank_withdraw"
    val isClarify: Boolean get() = action == "clarify"
}
