package com.roy.caloriebank.ui.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.ui.shared.DetailScaffold
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor
import com.roy.caloriebank.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    onAiProviderSettings: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    healthConnectViewModel: HealthConnectViewModel = hiltViewModel(),
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val useDynamicColor by themeViewModel.useDynamicColor.collectAsStateWithLifecycle()
    val healthState by healthConnectViewModel.uiState.collectAsStateWithLifecycle()
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted -> healthConnectViewModel.onPermissionsResult(granted) }

    DetailScaffold(title = "Settings", onBack = onBack) { padding ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Appearance", style = MaterialTheme.typography.titleMedium, color = TextSecondaryColor) }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceColor)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = TextSecondaryColor)
                    Text("Theme", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                }
                Spacer12()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ThemeOption(
                        label = "Light",
                        icon = Icons.Rounded.LightMode,
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { themeViewModel.setThemeMode(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f),
                    )
                    ThemeOption(
                        label = "Dark",
                        icon = Icons.Rounded.DarkMode,
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { themeViewModel.setThemeMode(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f),
                    )
                    ThemeOption(
                        label = "System",
                        icon = Icons.Rounded.PhoneAndroid,
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { themeViewModel.setThemeMode(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Spacer12()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Material You colors", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Use colors from your wallpaper instead of the app theme",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryColor,
                            )
                        }
                        Switch(
                            checked = useDynamicColor,
                            onCheckedChange = { themeViewModel.setUseDynamicColor(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor, checkedTrackColor = PrimaryColor.copy(alpha = 0.5f)),
                        )
                    }
                }
            }
        }

        item { Text("Health", style = MaterialTheme.typography.titleMedium, color = TextSecondaryColor) }
        item {
            SettingsRow(
                icon = Icons.Rounded.FavoriteBorder,
                title = if (healthState.isLinked) {
                    "Health Connect · ${healthState.steps} steps · ${healthState.activeCalories} kcal today"
                } else {
                    "Health Connect"
                },
                onClick = {
                    if (healthState.isLinked) {
                        healthConnectViewModel.requestSync()
                    } else {
                        healthPermissionLauncher.launch(healthConnectViewModel.permissions)
                    }
                },
                trailing = {
                    Text(
                        when {
                            !healthState.isAvailable -> "Unavailable"
                            healthState.isLinked -> "Connected"
                            else -> "Connect"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (healthState.isLinked) PrimaryColor else TextSecondaryColor,
                    )
                },
            )
        }

        item { Text("General", style = MaterialTheme.typography.titleMedium, color = TextSecondaryColor) }
        item {
            SettingsRow(
                icon = Icons.Rounded.AutoAwesome,
                title = "AI Provider",
                onClick = onAiProviderSettings,
                trailing = { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondaryColor) },
            )
        }

        item {
            SettingsRow(
                icon = Icons.Rounded.Notifications,
                title = "Notifications",
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor, checkedTrackColor = PrimaryColor.copy(alpha = 0.5f)),
                    )
                },
            )
        }

        item {
            SettingsRow(
                icon = Icons.Rounded.Info,
                title = "App Version",
                trailing = { Text("1.0.0", color = TextSecondaryColor) },
            )
        }
    }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextSecondaryColor)
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
        }
        trailing()
    }
}

@Composable
private fun Spacer12() {
    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 6.dp).height(6.dp))
}

@Composable
private fun ThemeOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PrimaryColor else MaterialTheme.colorScheme.background)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = if (selected) TextOnPrimaryColor else TextSecondaryColor)
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) TextOnPrimaryColor else TextSecondaryColor,
        )
    }
}
