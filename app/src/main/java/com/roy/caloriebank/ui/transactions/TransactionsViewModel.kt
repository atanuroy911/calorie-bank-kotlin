package com.roy.caloriebank.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.DailySummary
import com.roy.caloriebank.domain.model.isPositive
import com.roy.caloriebank.domain.repository.DailySummaryRepository
import com.roy.caloriebank.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class TransactionsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val transactions: List<CalorieTransaction> = emptyList(),
    val summary: DailySummary? = null,
    val earned: Int = 0,
    val spent: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    private val transactionRepository: TransactionRepository,
    private val dailySummaryRepository: DailySummaryRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<TransactionsUiState> = combine(
        preferencesRepository.userId,
        selectedDate,
    ) { userId, date -> userId to date }
        .flatMapLatest { (userId, date) ->
            if (userId == null) return@flatMapLatest emptyFlow<TransactionsUiState>()
            // Noon (rather than midnight) avoids landing on the wrong local day across a DST
            // transition when converted back to an Instant for the date-keyed queries below.
            val instant = date.atTime(LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant()
            combine(
                transactionRepository.watchTransactionsForDate(userId, instant),
                dailySummaryRepository.watchSummaryForDate(userId, instant),
            ) { txs, summary ->
                val earned = txs.filter { it.type.isPositive }.sumOf { it.calories }
                val spent = txs.filterNot { it.type.isPositive }.sumOf { it.calories }
                TransactionsUiState(
                    selectedDate = date,
                    transactions = txs,
                    summary = summary,
                    earned = earned,
                    spent = spent,
                    isLoading = false,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionsUiState(),
        )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun goToToday() {
        selectedDate.value = LocalDate.now()
    }
}
