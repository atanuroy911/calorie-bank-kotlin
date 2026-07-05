package com.roy.caloriebank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roy.caloriebank.ui.navigation.CalorieBankNavHost
import com.roy.caloriebank.ui.settings.ThemeViewModel
import com.roy.caloriebank.ui.theme.CalorieBankTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val useDynamicColor by themeViewModel.useDynamicColor.collectAsStateWithLifecycle()

            CalorieBankTheme(themeMode = themeMode, dynamicColor = useDynamicColor) {
                // Screens that don't wrap themselves in a Scaffold/Surface (e.g. manual-entry
                // forms) would otherwise show through to the window's default white background.
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CalorieBankNavHost()
                }
            }
        }
    }
}
