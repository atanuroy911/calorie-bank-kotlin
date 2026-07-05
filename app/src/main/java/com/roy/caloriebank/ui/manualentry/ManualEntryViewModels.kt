package com.roy.caloriebank.ui.manualentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.FoodCatalogEntity
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.data.repository.FoodCatalogRepository
import com.roy.caloriebank.domain.model.BankAccount
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.FoodEntry
import com.roy.caloriebank.domain.model.FoodItem
import com.roy.caloriebank.domain.model.Macros
import com.roy.caloriebank.domain.repository.BankRepository
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.FoodLogRepository
import com.roy.caloriebank.domain.repository.TransactionRepository
import com.roy.caloriebank.domain.repository.UserProfileRepository
import com.roy.caloriebank.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private fun dateKeyOf(instant: Instant): String =
    instant.atZone(ZoneId.systemDefault()).toLocalDate().toString()

@HiltViewModel
class ManualFoodEntryViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val foodLogRepository: FoodLogRepository,
    private val transactionRepository: TransactionRepository,
    private val dailySummaryRepository: DailySummaryRepository,
    private val userProfileRepository: UserProfileRepository,
    private val foodCatalogRepository: FoodCatalogRepository,
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _suggestions = MutableStateFlow<List<FoodCatalogEntity>>(emptyList())
    val suggestions: StateFlow<List<FoodCatalogEntity>> = _suggestions.asStateFlow()

    private val _isSearchingRemote = MutableStateFlow(false)
    val isSearchingRemote: StateFlow<Boolean> = _isSearchingRemote.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            foodCatalogRepository.seedIfNeeded()
            _suggestions.value = foodCatalogRepository.getRecentFoods()
        }
    }

    /** Local (instant) matches show immediately; if there are few of them, a debounced,
     * rate-limited Open Food Facts lookup fills in the rest a moment later. */
    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val local = foodCatalogRepository.searchLocal(query)
            _suggestions.value = local
            if (query.isBlank() || local.size >= 8) return@launch

            delay(500) // debounce so we don't fire a network call per keystroke
            _isSearchingRemote.value = true
            val remote = foodCatalogRepository.searchOpenFoodFacts(query)
            _isSearchingRemote.value = false
            if (remote != null) {
                val existingNames = local.map { it.name }.toSet()
                _suggestions.value = local + remote.filterNot { it.name in existingNames }
            }
        }
    }

    fun save(
        mealType: String,
        name: String,
        quantity: String,
        calories: Int,
        proteinG: Double,
        carbsG: Double,
        fatG: Double,
        onResult: (Boolean, String?) -> Unit,
    ) {
        if (name.isBlank()) {
            onResult(false, "Food name is required")
            return
        }
        viewModelScope.launch {
            val userId = preferencesRepository.userId.first()
            if (userId == null) {
                onResult(false, "Not signed in")
                return@launch
            }
            _isSaving.value = true
            try {
                val food = FoodItem(
                    name = name,
                    quantity = quantity.ifBlank { "1 serving" },
                    calories = calories,
                    macros = Macros(proteinG = proteinG, carbsG = carbsG, fatG = fatG),
                )
                val entry = FoodEntry.fromFoods(
                    id = UUID.randomUUID().toString(),
                    mealType = mealType,
                    foods = listOf(food),
                    userId = userId,
                )
                foodLogRepository.addFoodEntry(entry)
                transactionRepository.addTransaction(
                    CalorieTransaction(
                        id = UUID.randomUUID().toString(),
                        timestamp = entry.timestamp,
                        type = TransactionType.FoodWithdrawal,
                        calories = entry.totalCalories,
                        label = "${entry.mealTypeLabel} • ${food.name}",
                        foodEntryId = entry.id,
                        userId = userId,
                        date = dateKeyOf(entry.timestamp),
                    ),
                )
                val today = Instant.now()
                val existing = dailySummaryRepository.getSummaryForDate(userId, today)
                if (existing == null) {
                    val profile = userProfileRepository.getProfile(userId)
                    val budget = profile?.dailyCalorieBudget ?: 2000
                    dailySummaryRepository.saveSummary(
                        com.roy.caloriebank.domain.model.DailySummary(
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
                foodCatalogRepository.recordUsage(
                    name = name,
                    servingDescription = quantity.ifBlank { "1 serving" },
                    calories = calories,
                    proteinG = proteinG,
                    carbsG = carbsG,
                    fatG = fatG,
                )
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to save")
            } finally {
                _isSaving.value = false
            }
        }
    }
}

@HiltViewModel
class ManualExerciseEntryViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val transactionRepository: TransactionRepository,
    private val dailySummaryRepository: DailySummaryRepository,
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun save(
        exerciseName: String,
        durationMinutes: Int,
        caloriesBurned: Int,
        onResult: (Boolean, String?) -> Unit,
    ) {
        if (exerciseName.isBlank()) {
            onResult(false, "Exercise name is required")
            return
        }
        viewModelScope.launch {
            val userId = preferencesRepository.userId.first()
            if (userId == null) {
                onResult(false, "Not signed in")
                return@launch
            }
            _isSaving.value = true
            try {
                val timestamp = Instant.now()
                transactionRepository.addTransaction(
                    CalorieTransaction(
                        id = UUID.randomUUID().toString(),
                        timestamp = timestamp,
                        type = TransactionType.ExerciseDeposit,
                        calories = caloriesBurned,
                        label = "$exerciseName • $durationMinutes min",
                        userId = userId,
                        date = dateKeyOf(timestamp),
                    ),
                )
                val summary = dailySummaryRepository.getSummaryForDate(userId, timestamp)
                if (summary != null) {
                    dailySummaryRepository.updateSummary(
                        summary.copy(exerciseBonus = summary.exerciseBonus + caloriesBurned),
                    )
                }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to save")
            } finally {
                _isSaving.value = false
            }
        }
    }
}

@HiltViewModel
class ManualBankWithdrawViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val bankRepository: BankRepository,
    private val dailySummaryRepository: DailySummaryRepository,
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _bankAccount = MutableStateFlow<BankAccount?>(null)
    val bankAccount: StateFlow<BankAccount?> = _bankAccount.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = preferencesRepository.userId.first() ?: return@launch
            _bankAccount.value = bankRepository.getBankAccount(userId)
        }
    }

    fun save(amount: Int, reason: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val userId = preferencesRepository.userId.first()
            if (userId == null) {
                onResult(false, "Not signed in")
                return@launch
            }
            val bank = bankRepository.getBankAccount(userId)
            if (amount <= 0) {
                onResult(false, "Enter a valid amount")
                return@launch
            }
            if (amount > bank.balance) {
                onResult(false, "Amount exceeds bank balance")
                return@launch
            }
            _isSaving.value = true
            try {
                bankRepository.withdraw(userId, amount, reason.ifBlank { "Cheat meal" })
                val today = Instant.now()
                val summary = dailySummaryRepository.getSummaryForDate(userId, today)
                if (summary != null) {
                    dailySummaryRepository.updateSummary(summary.copy(bankBonus = summary.bankBonus + amount))
                }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Withdrawal failed")
            } finally {
                _isSaving.value = false
            }
        }
    }
}
