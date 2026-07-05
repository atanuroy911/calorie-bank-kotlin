package com.roy.caloriebank.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.roy.caloriebank.ui.theme.AppShapes
import com.roy.caloriebank.ui.theme.BackgroundColor
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.PrimaryGradient
import com.roy.caloriebank.ui.theme.SurfaceElevatedColor
import com.roy.caloriebank.ui.theme.TextMutedColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextPrimaryColor

/**
 * Standard scaffold for any screen pushed on top of the bottom-nav shell (nutrition detail,
 * manual entry forms, profile sub-screens). Guarantees every such screen always has a visible
 * way back, instead of relying solely on the system back gesture/button.
 */
@Composable
fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.background(BackgroundColor),
        containerColor = BackgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevatedColor)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimaryColor)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        },
    ) { padding -> content(padding) }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    gradient: Brush = PrimaryGradient,
) {
    val bg = if (enabled) gradient else Brush.linearGradient(listOf(TextMutedColor, TextMutedColor))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(AppShapes.buttonRadius))
            .background(bg)
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = TextOnPrimaryColor, modifier = Modifier.height(20.dp))
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = TextOnPrimaryColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryColor,
                modifier = Modifier.clickable(enabled = onActionClick != null) { onActionClick?.invoke() },
            )
        }
    }
}

@Composable
fun ComingSoonScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(PaddingValues(24.dp)), contentAlignment = Alignment.Center) {
        Text(text = "$title — coming soon", style = MaterialTheme.typography.bodyLarge)
    }
}
