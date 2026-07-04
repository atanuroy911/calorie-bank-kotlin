package com.roy.caloriebank.ui.onboarding

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.AccentColor
import com.roy.caloriebank.ui.theme.CarbsColor
import com.roy.caloriebank.ui.theme.FatColor
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.OnboardingBackgroundGradient
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.PrimaryGradient
import com.roy.caloriebank.ui.theme.ProteinColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor
import kotlin.math.roundToInt

private val activityLevels = listOf(
    "sedentary" to "Sedentary (desk job)",
    "light" to "Light (1-3 days/week)",
    "moderate" to "Moderate (3-5 days/week)",
    "active" to "Active (6-7 days/week)",
    "very_active" to "Very Active (physical job + exercise)",
)

private val goals = listOf(
    Triple("weight_loss", "🔥 Lose Weight", "-500 kcal/day"),
    Triple("maintenance", "⚖️ Maintain Weight", "No adjustment"),
    Triple("weight_gain", "💪 Gain Weight", "+300 kcal/day"),
)

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var validationError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingBackgroundGradient)
            .padding(20.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (state.currentPage > 0) {
                IconButton(onClick = { viewModel.prevPage() }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
            Spacer(Modifier.weight(1f))
            Text("${state.currentPage + 1} / 4", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryColor)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (state.currentPage + 1) / 4f },
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryColor,
        )
        Spacer(Modifier.height(24.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when (state.currentPage) {
                0 -> PersonalInfoPage(state, viewModel)
                1 -> BodyMetricsPage(state, viewModel)
                2 -> GoalPage(state, viewModel)
                3 -> SummaryPage(state)
            }
        }

        validationError?.let {
            Text(it, color = NegativeColor, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }

        GradientButton(
            text = if (state.currentPage == 3) "Get Started 🚀" else "Continue",
            isLoading = state.isSubmitting,
            onClick = {
                if (state.currentPage == 0 && state.name.isBlank()) {
                    validationError = "Please enter your name"
                    return@GradientButton
                }
                validationError = null
                if (state.currentPage == 3) {
                    viewModel.submit(onSuccess = onCompleted, onError = { validationError = it })
                } else {
                    viewModel.nextPage()
                }
            },
        )
    }
}

@Composable
private fun PersonalInfoPage(state: OnboardingState, vm: OnboardingViewModel) {
    Text("Personal Info", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = state.name,
        onValueChange = vm::updateName,
        label = { Text("Full Name") },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(20.dp))
    Text("Age", style = MaterialTheme.typography.titleMedium)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        StepperButton("-") { vm.updateAge(state.age - 1) }
        Text("${state.age}", style = MaterialTheme.typography.headlineMedium)
        StepperButton("+") { vm.updateAge(state.age + 1) }
    }
    Spacer(Modifier.height(20.dp))
    Text("Gender", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("male", "female", "other").forEach { g ->
            PillOption(label = g.replaceFirstChar { it.uppercase() }, selected = state.gender == g) { vm.updateGender(g) }
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(SurfaceColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun PillOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PrimaryColor else SurfaceColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(label, color = if (selected) TextOnPrimaryColor else TextSecondaryColor)
    }
}

@Composable
private fun BodyMetricsPage(state: OnboardingState, vm: OnboardingViewModel) {
    val isMetric = state.unitSystem == "metric"
    Text("Body Metrics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PillOption("Metric", isMetric) { vm.updateUnitSystem("metric") }
        PillOption("US", !isMetric) { vm.updateUnitSystem("us") }
    }
    Spacer(Modifier.height(20.dp))

    MetricSlider(
        label = "Height",
        value = state.heightCm,
        range = 140.0..220.0,
        isMetric = isMetric,
        unitMetric = "cm",
        toDisplay = { it },
        toUsDisplay = { cm -> cm / 2.54 },
        onChange = vm::updateHeight,
    )
    Spacer(Modifier.height(20.dp))
    MetricSlider(
        label = "Current Weight",
        value = state.currentWeightKg,
        range = 40.0..200.0,
        isMetric = isMetric,
        unitMetric = "kg",
        toDisplay = { it },
        toUsDisplay = { kg -> kg * 2.20462 },
        onChange = vm::updateCurrentWeight,
    )
    Spacer(Modifier.height(20.dp))
    MetricSlider(
        label = "Goal Weight",
        value = state.goalWeightKg,
        range = 40.0..200.0,
        isMetric = isMetric,
        unitMetric = "kg",
        toDisplay = { it },
        toUsDisplay = { kg -> kg * 2.20462 },
        onChange = vm::updateGoalWeight,
    )
}

@Composable
private fun MetricSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    isMetric: Boolean,
    unitMetric: String,
    toDisplay: (Double) -> Double,
    toUsDisplay: (Double) -> Double,
    onChange: (Double) -> Unit,
) {
    val displayValue = if (isMetric) toDisplay(value) else toUsDisplay(value)
    val unit = if (isMetric) unitMetric else if (unitMetric == "cm") "in" else "lbs"
    Text("$label: ${displayValue.roundToInt()} $unit", style = MaterialTheme.typography.titleMedium)
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.toDouble()) },
        valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = PrimaryColor, activeTrackColor = PrimaryColor),
    )
}

@Composable
private fun GoalPage(state: OnboardingState, vm: OnboardingViewModel) {
    Text("Goal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    goals.forEach { (value, label, subtitle) ->
        val selected = state.goal == value
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) PrimaryColor.copy(alpha = 0.2f) else SurfaceColor)
                .clickable { vm.updateGoal(value) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondaryColor)
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    Text("Activity Level", style = MaterialTheme.typography.titleMedium)
    activityLevels.forEach { (value, label) ->
        val selected = state.activityLevel == value
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) AccentColor.copy(alpha = 0.2f) else SurfaceColor)
                .clickable { vm.updateActivityLevel(value) }
                .padding(14.dp),
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SummaryPage(state: OnboardingState) {
    val goals = state.calculatedGoals
    Text("Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryGradient)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Daily Calorie Budget", color = TextOnPrimaryColor, style = MaterialTheme.typography.titleMedium)
        Text("${goals.dailyCalories} kcal", color = TextOnPrimaryColor, style = MaterialTheme.typography.displaySmall)
    }
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        MacroCard("Protein", "${goals.proteinG.roundToInt()}g", ProteinColor, Modifier.weight(1f))
        MacroCard("Carbs", "${goals.carbsG.roundToInt()}g", CarbsColor, Modifier.weight(1f))
        MacroCard("Fat", "${goals.fatG.roundToInt()}g", FatColor, Modifier.weight(1f))
    }
    Spacer(Modifier.height(20.dp))
    InfoRow("Goal", state.goal.replace("_", " ").replaceFirstChar { it.uppercase() })
    InfoRow("Activity", state.activityLevel.replace("_", " ").replaceFirstChar { it.uppercase() })
    InfoRow("Current weight", "${state.currentWeightKg.roundToInt()} kg")
    InfoRow("Target weight", "${state.goalWeightKg.roundToInt()} kg")
}

@Composable
private fun MacroCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryColor)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = TextSecondaryColor)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
