package com.roy.caloriebank.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.roy.caloriebank.domain.model.AiResponse
import com.roy.caloriebank.domain.model.ChatMessage
import com.roy.caloriebank.domain.model.ChatRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

private const val MODEL_NAME = "gemini-2.5-flash"

/**
 * CalBot's default text-out-of-the-box AI backend: Firebase AI Logic calling Gemini directly
 * from the client, gated by the app's own Firebase project (no user-supplied API key needed,
 * unlike the BYOK path). Requires app/google-services.json from a real Firebase project with the
 * Gemini Developer API (or Vertex AI) enabled — see AI_SETUP.md.
 */
@Singleton
class FirebaseAiDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val isConfigured: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        userContext: Map<String, Any?>?,
    ): AiResponse {
        check(isConfigured) {
            "Firebase AI Logic isn't set up yet — add app/google-services.json from a Firebase " +
                "project with the Gemini API enabled, or configure a BYOK provider key in Settings."
        }

        val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.7f
            },
            systemInstruction = content { text(buildSystemPrompt(userContext)) },
        )

        val historyContent = history
            .filter { it.role != ChatRole.System }
            .map { msg ->
                content(role = if (msg.role == ChatRole.User) "user" else "model") { text(msg.content) }
            }
        val turns = historyContent + content(role = "user") { text(userMessage) }

        val response = model.generateContent(turns)
        val raw = response.text ?: throw IllegalStateException("Empty response from Gemini")
        return parseResponse(raw)
    }

    private fun parseResponse(raw: String): AiResponse = try {
        val json = JSONObject(raw)
        AiResponse(
            message = json.optString("message", raw),
            action = json.optString("action", "none"),
            data = json.optJSONObject("data")?.toMap(),
        )
    } catch (e: Exception) {
        AiResponse(message = raw, action = "none", data = null)
    }
}

private fun JSONObject.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    keys().forEach { key -> map[key] = unwrap(get(key)) }
    return map
}

private fun JSONArray.toList(): List<Any?> = (0 until length()).map { unwrap(get(it)) }

private fun unwrap(value: Any?): Any? = when (value) {
    is JSONObject -> value.toMap()
    is JSONArray -> value.toList()
    JSONObject.NULL -> null
    else -> value
}

private fun buildSystemPrompt(userContext: Map<String, Any?>?): String = """
You are CalBot, the friendly nutrition assistant inside the Calorie Bank app. The user manages
calories like a bank account: a daily budget, calories consumed, calories remaining, and a savings
"bank" of surplus calories from past under-budget days.

${userContext?.let { "Current user context: $it" } ?: ""}

Reply to every message with ONLY a single JSON object (no markdown fences, no prose outside the
JSON) matching exactly this shape:
{
  "message": "<a short, friendly reply to show the user>",
  "action": "food_log" | "exercise_log" | "bank_withdraw" | "clarify" | "none",
  "data": { ... fields depending on action, or null }
}

Action contracts:
- "food_log": data = {
    "meal_type": "breakfast"|"lunch"|"dinner"|"snack",
    "foods": [ { "name": string, "quantity": string, "calories": number,
                 "protein_g": number, "carbs_g": number, "fat_g": number,
                 "fiber_g": number, "sugar_g": number } ]
  }
  Use when the user describes something they ate. Estimate reasonable nutrition values from your
  knowledge of typical foods; do not ask the user for macros they didn't provide.
- "exercise_log": data = { "exercise_name": string, "duration_minutes": number, "calories_burned": number }
  Use when the user describes exercise they did.
- "bank_withdraw": data = { "calories": number, "reason": string }
  Use only when the user explicitly asks to withdraw/use calories from their bank savings.
- "clarify": data = null. Use when you need more detail before logging anything (e.g. unclear
  portion size) — ask a short clarifying question in "message".
- "none": data = null. Use for general conversation, advice, or questions that don't log anything.

Keep "message" conversational and brief (1-3 sentences). Never wrap the JSON in markdown code
fences. Never include fields outside this schema.
""".trimIndent()
