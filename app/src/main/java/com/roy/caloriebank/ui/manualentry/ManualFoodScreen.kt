package com.roy.caloriebank.ui.manualentry

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.data.local.FoodCatalogEntity
import com.roy.caloriebank.data.local.FoodCatalogSource
import com.roy.caloriebank.ui.shared.DetailScaffold
import com.roy.caloriebank.ui.shared.GradientButton
import com.roy.caloriebank.ui.theme.NegativeColor
import com.roy.caloriebank.ui.theme.PrimaryColor
import com.roy.caloriebank.ui.theme.SurfaceElevatedColor
import com.roy.caloriebank.ui.theme.TextOnPrimaryColor
import com.roy.caloriebank.ui.theme.TextSecondaryColor

private val MEAL_TYPES = listOf("breakfast", "lunch", "dinner", "snack")

@Composable
fun ManualFoodScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit = onSaved,
    viewModel: ManualFoodEntryViewModel = hiltViewModel(),
) {
    var mealType by remember { mutableStateOf("breakfast") }
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1 serving") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("0") }
    var carbs by remember { mutableStateOf("0") }
    var fat by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuggestions by remember { mutableStateOf(false) }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val isSearchingRemote by viewModel.isSearchingRemote.collectAsStateWithLifecycle()

    fun applySuggestion(food: FoodCatalogEntity) {
        name = food.name
        quantity = food.servingDescription
        calories = food.calories.toString()
        protein = food.proteinG.toString()
        carbs = food.carbsG.toString()
        fat = food.fatG.toString()
        showSuggestions = false
    }

    DetailScaffold(title = "Log Food", onBack = onBack) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MEAL_TYPES.forEach { type ->
                val selected = mealType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) PrimaryColor else SurfaceElevatedColor)
                        .clickable { mealType = type }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) TextOnPrimaryColor else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                showSuggestions = true
                viewModel.onQueryChanged(it)
            },
            label = { Text("Food Name") },
            trailingIcon = {
                if (isSearchingRemote) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) showSuggestions = true },
        )

        if (showSuggestions && suggestions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevatedColor),
            ) {
                LazyColumn {
                    items(suggestions, key = { it.id }) { food ->
                        FoodSuggestionRow(food, onClick = { applySuggestion(food) })
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it.filter { c -> c.isDigit() } },
            label = { Text("Calories") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Protein (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text("Carbs (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fat (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        error?.let { Text(it, color = NegativeColor, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(16.dp))
        GradientButton(
            text = "Save",
            isLoading = isSaving,
            onClick = {
                showSuggestions = false
                viewModel.save(
                    mealType = mealType,
                    name = name,
                    quantity = quantity,
                    calories = calories.toIntOrNull() ?: 0,
                    proteinG = protein.toDoubleOrNull() ?: 0.0,
                    carbsG = carbs.toDoubleOrNull() ?: 0.0,
                    fatG = fat.toDoubleOrNull() ?: 0.0,
                ) { success, err ->
                    if (success) onSaved() else error = err
                }
            },
        )
    }
    }
}

@Composable
private fun FoodSuggestionRow(food: FoodCatalogEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (food.lastUsedAt != null) Icons.Rounded.History else Icons.Rounded.Restaurant,
            contentDescription = null,
            tint = TextSecondaryColor,
            modifier = Modifier.height(18.dp),
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(food.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${food.servingDescription} · ${food.calories} kcal" +
                    if (food.source == FoodCatalogSource.OPEN_FOOD_FACTS) " · Open Food Facts" else "",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryColor,
            )
        }
    }
}
