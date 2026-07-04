package com.roy.caloriebank.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.PositiveColor
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

@Composable
fun AiProviderSettingsScreen(
    viewModel: AiProviderSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("AI Provider Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        // Section 1: Your Backend Server
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceColor)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Your Backend Server", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = state.useCustomBackend,
                    onCheckedChange = viewModel::setUseCustomBackend,
                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor),
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.backendUrl,
                onValueChange = viewModel::setBackendUrl,
                label = { Text("Backend URL") },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.useCustomBackend,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.backendToken,
                onValueChange = viewModel::setBackendToken,
                label = { Text("Bearer Token") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.useCustomBackend,
            )
            Spacer(Modifier.height(12.dp))
            GradientButton(
                text = "Test Connection",
                isLoading = state.isTesting,
                enabled = state.useCustomBackend && state.backendUrl.isNotBlank(),
                onClick = { viewModel.testConnection() },
            )
            state.testResult?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = if (it.startsWith("Connected")) PositiveColor else TextSecondaryColor)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Section 2: Direct Provider fallback
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceColor)
                .padding(16.dp)
                .alpha(if (state.useCustomBackend) 0.45f else 1f),
        ) {
            Text("Direct Provider (fallback)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                supportedAiProviders.forEach { p ->
                    val selected = state.provider == p
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) PrimaryColor else SurfaceColor)
                            .clickable(enabled = !state.useCustomBackend) { viewModel.setProvider(p) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(p, color = if (selected) TextOnPrimaryColor else TextSecondaryColor)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = viewModel::setApiKey,
                label = { Text("API Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.useCustomBackend,
            )
        }

        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = if (state.saved) "✓ Saved!" else "Save Settings",
            isLoading = state.isSaving,
            gradient = if (state.saved) androidx.compose.ui.graphics.Brush.linearGradient(listOf(PositiveColor, PositiveColor)) else com.roy.caloriebank.ui.theme.PrimaryGradient,
            onClick = { viewModel.save() },
        )
    }
}
