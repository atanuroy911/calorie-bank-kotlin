package com.roy.caloriebank.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.BankAccount
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.repository.BankRepository
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BankUiState(
    val bankAccount: BankAccount? = null,
    // "Recent Bank Activity" intentionally mirrors today's transactions filtered client-side,
    // matching the original app's behavior (see report section 3 / gap #6).
    val bankTransactionsToday: List<CalorieTransaction> = emptyList(),
    val isWithdrawing: Boolean = false,
    val withdrawError: String? = null,
)

@HiltViewModel
class BankViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val bankRepository: BankRepository,
    private val transactionRepository: TransactionRepository,
    private val dailySummaryRepository: DailySummaryRepository,
) : ViewModel() {

    val uiState: StateFlow<BankUiState> = preferencesRepository.userId.flatMapLatest { userId ->
        if (userId == null) return@flatMapLatest emptyFlow<BankUiState>()
        val now = Instant.now()
        combine(
            bankRepository.watchBankAccount(userId),
            transactionRepository.watchTransactionsForDate(userId, now),
        ) { bank, txs ->
            BankUiState(
                bankAccount = bank,
                bankTransactionsToday = txs.filter {
                    it.type.name.contains("Bank") || it.type.name.contains("Savings")
                },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BankUiState(),
    )

    private val _isWithdrawing = MutableStateFlow(false)
    val isWithdrawing: StateFlow<Boolean> = _isWithdrawing.asStateFlow()

    fun withdraw(amount: Int, reason: String, onResult: (Boolean, String?) -> Unit) {
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
            _isWithdrawing.value = true
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
                _isWithdrawing.value = false
            }
        }
    }
}
