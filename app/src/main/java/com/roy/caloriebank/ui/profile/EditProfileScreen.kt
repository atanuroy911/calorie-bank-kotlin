package com.roy.caloriebank.ui.profile

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.SurfaceColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor
import kotlin.math.roundToInt

private val activityLevels = listOf(
    "sedentary" to "Sedentary", "light" to "Light", "moderate" to "Moderate",
    "active" to "Active", "very_active" to "Very Active",
)
private val goalOptions = listOf("weight_loss" to "Lose", "maintenance" to "Maintain", "weight_gain" to "Gain")

@Composable
fun EditProfileScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit = onSaved,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailScaffold(title = "Edit Profile", onBack = onBack) { padding ->
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return@DetailScaffold
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::updateName,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        Text("Age", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Stepper("-") { viewModel.updateAge(state.age - 1) }
            Text("${state.age}", style = MaterialTheme.typography.headlineSmall)
            Stepper("+") { viewModel.updateAge(state.age + 1) }
        }
        Spacer(Modifier.height(16.dp))

        Text("Gender", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("male", "female", "other").forEach { g ->
                Pill(g.replaceFirstChar { it.uppercase() }, state.gender == g) { viewModel.updateGender(g) }
            }
        }
        Spacer(Modifier.height(20.dp))

        LabeledSlider("Height", state.heightCm, 140.0..220.0, "cm") { viewModel.updateHeight(it) }
        Spacer(Modifier.height(16.dp))
        LabeledSlider("Current Weight", state.currentWeightKg, 40.0..200.0, "kg") { viewModel.updateCurrentWeight(it) }
        Spacer(Modifier.height(16.dp))
        LabeledSlider("Goal Weight", state.goalWeightKg, 40.0..200.0, "kg") { viewModel.updateGoalWeight(it) }
        Spacer(Modifier.height(20.dp))

        Text("Goal", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            goalOptions.forEach { (value, label) ->
                Pill(label, state.goal == value) { viewModel.updateGoal(value) }
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Activity Level", style = MaterialTheme.typography.titleMedium)
        activityLevels.forEach { (value, label) ->
            val selected = state.activityLevel == value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) PrimaryColor.copy(alpha = 0.2f) else SurfaceColor)
                    .clickable { viewModel.updateActivityLevel(value) }
                    .padding(12.dp),
            ) {
                Text(label)
            }
        }

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = NegativeColor, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = "Save Changes",
            isLoading = state.isSaving,
            onClick = { viewModel.save(onSaved) },
        )
    }
    }
}

@Composable
private fun Stepper(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SurfaceColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PrimaryColor else SurfaceColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, color = if (selected) TextOnPrimaryColor else TextSecondaryColor)
    }
}

@Composable
private fun LabeledSlider(label: String, value: Double, range: ClosedFloatingPointRange<Double>, unit: String, onChange: (Double) -> Unit) {
    Text("$label: ${value.roundToInt()} $unit", style = MaterialTheme.typography.titleMedium)
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.toDouble()) },
        valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = PrimaryColor, activeTrackColor = PrimaryColor),
    )
}
