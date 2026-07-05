package com.roy.caloriebank.ui.manualentry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.domain.util.kcalFormatted
import com.roy.caloriebank.ui.shared.DetailScaffold
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.AccentGradient
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor

@Composable
fun ManualBankWithdrawScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit = onSaved,
    viewModel: ManualBankWithdrawViewModel = hiltViewModel(),
) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Cheat meal") }
    var error by remember { mutableStateOf<String?>(null) }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val bankAccount by viewModel.bankAccount.collectAsStateWithLifecycle()

    DetailScaffold(title = "Withdraw from Bank", onBack = onBack) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AccentGradient)
                .padding(16.dp),
        ) {
            Text("Available Balance", style = MaterialTheme.typography.titleSmall, color = TextOnPrimaryColor)
            Text(
                "${(bankAccount?.balance ?: 0).kcalFormatted()} kcal",
                style = MaterialTheme.typography.headlineLarge,
                color = TextOnPrimaryColor,
            )
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() } },
            label = { Text("Withdraw Amount (kcal)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        error?.let { Text(it, color = NegativeColor, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(16.dp))
        GradientButton(
            text = "Withdraw",
            isLoading = isSaving,
            gradient = AccentGradient,
            onClick = {
                val value = amount.toIntOrNull()
                if (value == null || value <= 0) {
                    error = "Enter a valid amount"
                    return@GradientButton
                }
                viewModel.save(value, reason) { success, err ->
                    if (success) onSaved() else error = err
                }
            },
        )
    }
    }
}
