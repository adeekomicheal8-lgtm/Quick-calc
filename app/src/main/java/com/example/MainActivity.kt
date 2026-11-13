package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CalculatorRoute
import com.example.ui.CalculatorViewModel
import com.example.ui.ThemePreference
import com.example.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: CalculatorViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      val darkTheme =
        when (uiState.themePreference) {
          ThemePreference.SYSTEM -> isSystemInDarkTheme()
          ThemePreference.DARK -> true
          ThemePreference.LIGHT -> false
        }

      CalculatorTheme(darkTheme = darkTheme) {
        CalculatorRoute(viewModel = viewModel)
      }
    }
  }
}
