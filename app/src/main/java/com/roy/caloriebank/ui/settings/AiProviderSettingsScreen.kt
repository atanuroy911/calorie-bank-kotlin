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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.ui.shared.DetailScaffold
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.AccentColor
import com.roy.caloriebank.ui.theme.PositiveColor
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

private val apiKeyPortalUrls = mapOf(
    "gemini" to "https://aistudio.google.com/apikey",
    "openai" to "https://platform.openai.com/api-keys",
    "claude" to "https://console.anthropic.com/settings/keys",
    "openrouter" to "https://openrouter.ai/keys",
)

@Composable
fun AiProviderSettingsScreen(
    onBack: () -> Unit,
    viewModel: AiProviderSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    DetailScaffold(title = "AI Provider Settings", onBack = onBack) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        // Explains the BYOK (bring-your-own-key) model up front — without this, "why do I need to
        // paste in an API key?" is a confusing first impression for a calorie-tracking app.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AccentColor.copy(alpha = 0.12f))
                .padding(14.dp),
        ) {
            Icon(Icons.Rounded.Shield, contentDescription = null, tint = AccentColor)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("You bring your own AI key", style = MaterialTheme.typography.titleSmall, color = AccentColor)
                Spacer(Modifier.height(4.dp))
                Text(
                    "CalBot connects directly to a provider you choose using your own API key, billed to " +
                        "your own account with that provider. Your key is stored only on this device and is " +
                        "never sent to us.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryColor,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Key, contentDescription = null, tint = TextSecondaryColor)
                Text(
                    "Bring Your Own Key (BYOK)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (state.useCustomBackend) {
                    "Disabled while a custom backend is configured above."
                } else {
                    "Pick a provider and paste in your personal API key."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryColor,
            )
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
            apiKeyPortalUrls[state.provider]?.let { url ->
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = !state.useCustomBackend) { uriHandler.openUri(url) },
                ) {
                    Text(
                        "Get a free API key from ${state.provider.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.useCustomBackend) TextSecondaryColor else PrimaryColor,
                    )
                    Icon(
                        Icons.Rounded.OpenInNew,
                        contentDescription = null,
                        tint = if (state.useCustomBackend) TextSecondaryColor else PrimaryColor,
                        modifier = Modifier.padding(start = 4.dp).height(14.dp),
                    )
                }
            }
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
}
