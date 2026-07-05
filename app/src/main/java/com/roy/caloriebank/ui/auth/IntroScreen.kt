package com.roy.caloriebank.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.AccentColor
import com.roy.caloriebank.ui.theme.LoginBackgroundGradient
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.ProteinColor
import com.roy.caloriebank.ui.theme.TextMutedColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor
import kotlinx.coroutines.launch

private data class IntroPage(val emoji: String, val title: String, val subtitle: String, val color: androidx.compose.ui.graphics.Color)

private val introPages = listOf(
    IntroPage("🏦", "Welcome to Calorie Bank", "Manage your calories just like money in a bank account.", PrimaryColor),
    IntroPage("💰", "Save for later", "Eat less today, save the surplus to spend on a weekend feast.", AccentColor),
    IntroPage("🤖", "Log with AI", "Just tell CalBot what you ate. No more searching for items.", ProteinColor),
)

@Composable
fun IntroScreen(
    onFinished: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { introPages.size })
    val scope = rememberCoroutineScopeCompat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBackgroundGradient)
            .padding(24.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            val item = introPages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .background(item.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(item.emoji, style = MaterialTheme.typography.displayLarge)
                }
                androidx.compose.foundation.layout.Spacer(Modifier.height(32.dp))
                Text(
                    item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = item.color,
                    textAlign = TextAlign.Center,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondaryColor,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            introPages.indices.forEach { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (selected) PrimaryColor else TextMutedColor),
                )
            }
        }

        val isLastPage = pagerState.currentPage == introPages.lastIndex
        GradientButton(
            text = if (isLastPage) "Get Started" else "Continue",
            onClick = {
                if (isLastPage) {
                    viewModel.finishIntro(onFinished)
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
        )
    }
}

@Composable
private fun rememberCoroutineScopeCompat() = androidx.compose.runtime.rememberCoroutineScope()
