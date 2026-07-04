package com.roy.caloriebank.ui.chat

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.domain.model.ChatMessage
import com.roy.caloriebank.domain.util.timeLabel
import com.roy.caloriebank.ui.theme.AccentColor
import com.roy.caloriebank.ui.theme.AccentGradient
import com.roy.caloriebank.ui.theme.PositiveColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.SurfaceElevatedColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

private val SUGGESTIONS = listOf(
    "I had a chicken salad for lunch",
    "I ran for 30 minutes",
    "Withdraw 300 calories from my bank",
    "What should I eat for dinner?",
)

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        bottomBar = {
            ChatInputBar(
                value = input,
                onValueChange = { input = it },
                isSending = uiState.isSending,
                onSend = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input)
                        input = ""
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.messages.isEmpty()) {
            EmptyChatState(
                modifier = Modifier.fillMaxSize().padding(padding),
                onSuggestionClick = { viewModel.sendMessage(it) },
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.messages, key = { it.id }) { msg -> ChatBubble(msg) }
                if (uiState.isSending) {
                    item { TypingIndicator() }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(modifier: Modifier = Modifier, onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = AccentColor, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text("Hi! I'm CalBot 👋", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Tell me what you ate or did, and I'll log it for you.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryColor,
        )
        Spacer(Modifier.height(20.dp))
        SUGGESTIONS.forEach { suggestion ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceElevatedColor)
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(12.dp),
            ) {
                Text(suggestion, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isUser) AccentColor.copy(alpha = 0.25f) else SurfaceElevatedColor)
                .padding(12.dp),
        ) {
            Text(message.content, style = MaterialTheme.typography.bodyMedium)
            if (message.hasFoodLog || message.hasExerciseLog) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = PositiveColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        if (message.hasFoodLog) "Logged to your account" else "Exercise logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = PositiveColor,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
            Text(
                message.timestamp.timeLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevatedColor)
            .padding(12.dp),
    ) {
        Text("CalBot is typing…", style = MaterialTheme.typography.bodySmall, color = TextSecondaryColor)
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Tell CalBot what you ate…") },
            maxLines = 4,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AccentGradient)
                .clickable(enabled = !isSending) { onSend() },
            contentAlignment = Alignment.Center,
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Icon(Icons.Rounded.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}
