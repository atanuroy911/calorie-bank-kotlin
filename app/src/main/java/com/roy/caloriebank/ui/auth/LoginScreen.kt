package com.roy.caloriebank.ui.auth

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.LoginBackgroundGradient
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.PrimaryGradient
import com.roy.caloriebank.ui.theme.TextMutedColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBackgroundGradient)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(PrimaryGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = TextOnPrimaryColor,
                modifier = Modifier.size(44.dp),
            )
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
        Text(
            "Welcome to Calorie Bank",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        Text(
            "Your personal calorie savings account.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondaryColor,
            textAlign = TextAlign.Center,
        )

        androidx.compose.foundation.layout.Spacer(Modifier.height(48.dp))

        uiState.error?.let {
            Text(it, color = NegativeColor, style = MaterialTheme.typography.bodySmall)
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable(enabled = !uiState.isLoading) { viewModel.signInWithGoogle(onLoggedIn) },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                Text("Continue with Google", color = Color.Black, style = MaterialTheme.typography.labelLarge)
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

        Text(
            "Login later (Continue as Guest)",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryColor,
            modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                viewModel.continueAsGuest(onLoggedIn)
            },
        )

        androidx.compose.foundation.layout.Spacer(Modifier.height(32.dp))

        Text(
            "By continuing you agree to our Terms & Privacy Policy.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedColor,
            textAlign = TextAlign.Center,
        )
    }
}
