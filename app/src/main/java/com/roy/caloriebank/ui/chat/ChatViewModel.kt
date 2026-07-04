package com.roy.caloriebank.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.ChatMessage
import com.roy.caloriebank.domain.model.ChatMessageStatus
import com.roy.caloriebank.domain.model.ChatRole
import com.roy.caloriebank.domain.model.FoodItem
import com.roy.caloriebank.domain.model.Macros
import com.roy.caloriebank.domain.model.Micros
import com.roy.caloriebank.domain.model.FoodEntry
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.TransactionType
import com.roy.caloriebank.domain.repository.AiRepository
import com.roy.caloriebank.domain.repository.BankRepository
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.FoodLogRepository
import com.roy.caloriebank.domain.repository.TransactionRepository
import com.roy.caloriebank.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChatUiState(
    val sessionId: String = UUID.randomUUID().toString(),
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null,
)

private fun dateKeyOf(instant: Instant): String =
    instant.atZone(ZoneId.systemDefault()).toLocalDate().toString()

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val aiRepository: AiRepository,
    private val foodLogRepository: FoodLogRepository,
    private val transactionRepository: TransactionRepository,
    private val bankRepository: BankRepository,
    private val dailySummaryRepository: DailySummaryRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            val state = _uiState.value
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = state.sessionId,
                role = ChatRole.User,
                content = trimmed,
                timestamp = Instant.now(),
                status = ChatMessageStatus.Sent,
            )
            _uiState.value = state.copy(messages = state.messages + userMessage, isSending = true)

            val userId = preferencesRepository.userId.first()
            if (userId == null) {
                appendAssistant("Please sign in first.", isError = true)
                return@launch
            }

            val profile = userProfileRepository.getProfile(userId)
            val limit = if (profile?.isPremium == true) 200 else 10
            val usageToday = preferencesRepository.aiUsageToday.first()
            if (usageToday >= limit) {
                appendAssistant(
                    "You've reached your daily AI message limit ($limit). Try again tomorrow, or upgrade to Premium for more.",
                )
                return@launch
            }

            val today = Instant.now()
            val summary = dailySummaryRepository.getSummaryForDate(userId, today)
            val bank = bankRepository.getBankAccount(userId)
            val userContext = mapOf(
                "daily_budget" to (summary?.budget ?: 0),
                "consumed_today" to (summary?.consumed ?: 0),
                "remaining_today" to (summary?.remaining ?: 0),
                "bank_balance" to bank.balance,
                "user_id" to userId,
            )

            try {
                val response = aiRepository.sendMessage(
                    sessionId = state.sessionId,
                    history = state.messages,
                    userMessage = trimmed,
                    userContext = userContext,
                )
                preferencesRepository.incrementAiUsage()

                var hasFoodLog = false
                var hasExerciseLog = false
                when (response.action) {
                    "food_log" -> hasFoodLog = processFoodLog(userId, response.data)
                    "exercise_log" -> hasExerciseLog = processExerciseLog(userId, response.data)
                    "bank_withdraw" -> processBankWithdraw(userId, response.data)
                }

                appendAssistant(response.message, hasFoodLog = hasFoodLog, hasExerciseLog = hasExerciseLog)
            } catch (e: Exception) {
                val msg = (e.message ?: "Something went wrong").removePrefix("Exception:").trim()
                appendAssistant("❌ $msg", isError = true)
            }
        }
    }

    private fun appendAssistant(
        text: String,
        hasFoodLog: Boolean = false,
        hasExerciseLog: Boolean = false,
        isError: Boolean = false,
    ) {
        val state = _uiState.value
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = state.sessionId,
            role = ChatRole.Assistant,
            content = text,
            timestamp = Instant.now(),
            status = if (isError) ChatMessageStatus.Error else ChatMessageStatus.Sent,
            hasFoodLog = hasFoodLog,
            hasExerciseLog = hasExerciseLog,
        )
        _uiState.value = state.copy(messages = state.messages + message, isSending = false)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processFoodLog(userId: String, data: Map<String, Any?>?): Boolean = try {
        val mealType = (data?.get("meal_type") as? String) ?: "snack"
        val foodsRaw = (data?.get("foods") as? List<Map<String, Any?>>) ?: emptyList()
        val foods = foodsRaw.map { f ->
            FoodItem(
                name = (f["name"] as? String) ?: "",
                quantity = (f["quantity"] as? String) ?: "",
                calories = numOf(f["calories"]).toInt(),
                macros = Macros(
                    proteinG = numOf(f["protein_g"]),
                    carbsG = numOf(f["carbs_g"]),
                    fatG = numOf(f["fat_g"]),
                    fiberG = numOf(f["fiber_g"]),
                    sugarG = numOf(f["sugar_g"]),
                    saturatedFatG = numOf(f["saturated_fat_g"]),
                    transFatG = numOf(f["trans_fat_g"]),
                    cholesterolMg = numOf(f["cholesterol_mg"]),
                ),
                micros = Micros(
                    sodiumMg = numOf(f["sodium_mg"]),
                    potassiumMg = numOf(f["potassium_mg"]),
                    calciumMg = numOf(f["calcium_mg"]),
                    ironMg = numOf(f["iron_mg"]),
                    magnesiumMg = numOf(f["magnesium_mg"]),
                    zincMg = numOf(f["zinc_mg"]),
                    phosphorusMg = numOf(f["phosphorus_mg"]),
                    vitaminCMg = numOf(f["vitamin_c_mg"]),
                    vitaminDUg = numOf(f["vitamin_d_ug"]),
                    vitaminB12Ug = numOf(f["vitamin_b12_ug"]),
                    folateMcg = numOf(f["folate_mcg"]),
                    vitaminAUg = numOf(f["vitamin_a_ug"]),
                    vitaminEMg = numOf(f["vitamin_e_mg"]),
                    vitaminKUg = numOf(f["vitamin_k_ug"]),
                ),
            )
        }
        val entry = FoodEntry.fromFoods(
            id = UUID.randomUUID().toString(),
            mealType = mealType,
            foods = foods,
            aiSessionId = _uiState.value.sessionId,
            userId = userId,
        )
        foodLogRepository.addFoodEntry(entry)
        val label = "${entry.mealTypeLabel} • ${foods.joinToString(", ") { it.name }}"
        transactionRepository.addTransaction(
            CalorieTransaction(
                id = UUID.randomUUID().toString(),
                timestamp = entry.timestamp,
                type = TransactionType.FoodWithdrawal,
                calories = entry.totalCalories,
                label = label,
                foodEntryId = entry.id,
                userId = userId,
                date = dateKeyOf(entry.timestamp),
            ),
        )
        updateDailySummaryForFood(userId, entry)
        true
    } catch (e: Exception) {
        false
    }

    private suspend fun updateDailySummaryForFood(userId: String, entry: FoodEntry) {
        val today = Instant.now()
        val existing = dailySummaryRepository.getSummaryForDate(userId, today)
        if (existing == null) {
            val profile = userProfileRepository.getProfile(userId)
            val budget = profile?.dailyCalorieBudget ?: 2000
            dailySummaryRepository.saveSummary(
                DailySummary(
                    userId = userId,
                    date = today,
                    budget = budget,
                    consumed = entry.totalCalories,
                    macros = entry.totalMacros,
                    micros = entry.totalMicros,
                ),
            )
        } else {
            dailySummaryRepository.updateSummary(
                existing.copy(
                    consumed = existing.consumed + entry.totalCalories,
                    macros = existing.macros + entry.totalMacros,
                    micros = existing.micros + entry.totalMicros,
                ),
            )
        }
    }

    private suspend fun processExerciseLog(userId: String, data: Map<String, Any?>?): Boolean = try {
        val name = (data?.get("exercise_name") as? String) ?: "Exercise"
        val duration = numOf(data?.get("duration_minutes")).toInt()
        val caloriesBurned = numOf(data?.get("calories_burned")).toInt()
        transactionRepository.addTransaction(
            CalorieTransaction(
                id = UUID.randomUUID().toString(),
                timestamp = Instant.now(),
                type = TransactionType.ExerciseDeposit,
                calories = caloriesBurned,
                label = "$name • $duration min",
                userId = userId,
                date = dateKeyOf(Instant.now()),
            ),
        )
        val today = Instant.now()
        val summary = dailySummaryRepository.getSummaryForDate(userId, today)
        if (summary != null) {
            dailySummaryRepository.updateSummary(summary.copy(exerciseBonus = summary.exerciseBonus + caloriesBurned))
        }
        true
    } catch (e: Exception) {
        false
    }

    private suspend fun processBankWithdraw(userId: String, data: Map<String, Any?>?): Boolean = try {
        val calories = numOf(data?.get("calories")).toInt()
        val reason = (data?.get("reason") as? String) ?: "Bank Withdrawal"
        bankRepository.withdraw(userId, calories, reason)
        val today = Instant.now()
        val summary = dailySummaryRepository.getSummaryForDate(userId, today)
        if (summary != null) {
            dailySummaryRepository.updateSummary(summary.copy(bankBonus = summary.bankBonus + calories))
        }
        true
    } catch (e: Exception) {
        false
    }

    private fun numOf(v: Any?): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}
