package com.roy.caloriebank.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.domain.util.kcalFormatted
import com.roy.caloriebank.ui.theme.AccentColor
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.PositiveColor
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.PrimaryGradient
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onPremium: () -> Unit,
    onAiSettings: () -> Unit,
    onSettings: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PrimaryGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = TextOnPrimaryColor, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(profile?.displayName ?: "Calorie Banker", style = MaterialTheme.typography.titleLarge)
                Text(profile?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryColor)
                if (profile?.isPremium == true) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentColor)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text("Premium", color = TextOnPrimaryColor, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Today", "${uiState.summary?.consumed ?: 0}", PrimaryColor, Modifier.weight(1f))
                StatCard("Bank", "${uiState.bankAccount?.balance ?: 0}", AccentColor, Modifier.weight(1f))
                StatCard("Budget", "${profile?.dailyCalorieBudget?.kcalFormatted() ?: "0"}", PositiveColor, Modifier.weight(1f))
            }
        }

        item {
            BadgesCard(currentStreak = uiState.currentStreak, longestStreak = uiState.longestStreak, bankBalance = uiState.bankAccount?.balance ?: 0)
        }

        item {
            MenuItem(Icons.Rounded.Edit, "Edit Profile", onClick = onEditProfile)
        }
        item {
            MenuItem(Icons.Rounded.Star, "Upgrade to Premium", tint = AccentColor, onClick = onPremium)
        }
        item {
            MenuItem(Icons.Rounded.AutoAwesome, "AI Provider Settings", onClick = onAiSettings)
        }
        item {
            MenuItem(Icons.Rounded.Settings, "Settings", onClick = onSettings)
        }
        item {
            MenuItem(
                Icons.AutoMirrored.Rounded.Logout,
                "Sign Out",
                tint = NegativeColor,
                onClick = { viewModel.signOut(onSignedOut) },
            )
        }
    }
}

private data class Badge(val emoji: String, val label: String, val unlocked: Boolean)

@Composable
private fun BadgesCard(currentStreak: Int, longestStreak: Int, bankBalance: Int) {
    val badges = listOf(
        Badge("🔥", "3-Day Streak", longestStreak >= 3),
        Badge("⚡", "7-Day Streak", longestStreak >= 7),
        Badge("🏆", "30-Day Streak", longestStreak >= 30),
        Badge("🐷", "1000 kcal Saved", bankBalance >= 1000),
        Badge("💎", "5000 kcal Saved", bankBalance >= 5000),
        Badge("👑", "10000 kcal Saved", bankBalance >= 10000),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Badges", style = MaterialTheme.typography.titleMedium)
            if (currentStreak > 0) {
                Text("🔥 $currentStreak day streak", style = MaterialTheme.typography.labelMedium, color = PrimaryColor)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            badges.take(3).forEach { BadgeTile(it, Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            badges.drop(3).forEach { BadgeTile(it, Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun BadgeTile(badge: Badge, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (badge.unlocked) PrimaryColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.background)
            .padding(vertical = 12.dp)
            .then(if (badge.unlocked) Modifier else Modifier.alpha(0.35f)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(badge.emoji, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            badge.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (badge.unlocked) MaterialTheme.colorScheme.onSurface else TextSecondaryColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryColor)
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = TextSecondaryColor,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(Modifier.height(0.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondaryColor)
    }
}
