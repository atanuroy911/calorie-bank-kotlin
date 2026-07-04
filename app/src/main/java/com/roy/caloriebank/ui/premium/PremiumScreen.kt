package com.roy.caloriebank.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.AccentGradient
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor
import kotlinx.coroutines.launch

private data class Feature(val icon: ImageVector, val title: String)

private val features = listOf(
    Feature(Icons.Rounded.Star, "Unlimited AI Messages"),
    Feature(Icons.Rounded.NotificationsOff, "No Advertisements"),
    Feature(Icons.Rounded.Insights, "Advanced Analytics (weekly/monthly trends)"),
    Feature(Icons.Rounded.CloudDone, "Cloud Backup"),
    Feature(Icons.Rounded.SupportAgent, "Priority Support"),
)

@Composable
fun PremiumScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentGradient)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = TextOnPrimaryColor, modifier = Modifier.height(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("Premium", style = MaterialTheme.typography.headlineMedium, color = TextOnPrimaryColor)
                Text(
                    "Unlimited AI logging, no ads, full access",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnPrimaryColor,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))

            features.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor)
                        .padding(16.dp)
                        .padding(bottom = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(feature.icon, contentDescription = null)
                    Text(feature.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            GradientButton(
                text = "Subscribe — \$4.99 / month",
                gradient = AccentGradient,
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Play Billing integration coming soon")
                    }
                },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Cancel anytime. Billed monthly via Google Play.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}
