package com.roy.caloriebank.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy.caloriebank.data.local.PreferencesRepository
import com.roy.caloriebank.domain.model.CalorieTransaction
import com.roy.caloriebank.domain.model.isPositive
import com.roy.caloriebank.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TransactionsUiState(
    val transactions: List<CalorieTransaction> = emptyList(),
    val earned: Int = 0,
    val spent: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    transactionRepository: TransactionRepository,
) : ViewModel() {

    val uiState: StateFlow<TransactionsUiState> = preferencesRepository.userId.flatMapLatest { userId ->
        if (userId == null) return@flatMapLatest emptyFlow<TransactionsUiState>()
        transactionRepository.watchTransactionsForDate(userId, Instant.now()).map { txs ->
            val earned = txs.filter { it.type.isPositive }.sumOf { it.calories }
            val spent = txs.filterNot { it.type.isPositive }.sumOf { it.calories }
            TransactionsUiState(transactions = txs, earned = earned, spent = spent, isLoading = false)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState(),
    )
}
