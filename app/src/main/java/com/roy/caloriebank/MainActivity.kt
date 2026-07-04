package com.roy.caloriebank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.roy.caloriebank.ui.navigation.CalorieBankNavHost
import com.roy.caloriebank.ui.theme.CalorieBankTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalorieBankTheme {
                // Screens that don't wrap themselves in a Scaffold/Surface (e.g. manual-entry
                // forms) would otherwise show through to the window's default white background.
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CalorieBankNavHost()
                }
            }
        }
    }
}
