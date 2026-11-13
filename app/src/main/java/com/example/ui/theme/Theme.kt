package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape

private val DarkColorScheme =
  darkColorScheme(
    primary = LuminousTangerine,
    onPrimary = Color.White,
    primaryContainer = EmberContainerDark,
    onPrimaryContainer = OnEmberContainerDark,
    secondary = MintTealDark,
    onSecondary = DeepObsidian,
    secondaryContainer = TealContainerDark,
    onSecondaryContainer = OnTealContainerDark,
    tertiary = CoolSilver,
    onTertiary = DeepObsidian,
    background = DeepObsidian,
    onBackground = WarmWhite,
    surface = GraphiteSurface,
    onSurface = WarmWhite,
    surfaceVariant = ElevatedCharcoal,
    onSurfaceVariant = CoolSilver,
    surfaceContainerLowest = DeepObsidian,
    surfaceContainerLow = GraphiteSurface,
    surfaceContainer = DarkSlateKey,
    surfaceContainerHigh = ElevatedCharcoal,
    surfaceContainerHighest = HairlineSlate,
    outline = CoolSilver,
    outlineVariant = HairlineSlate,
    error = ErrorRedDark,
    onError = DeepObsidian,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorRedDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = VermilionLight,
    onPrimary = Color.White,
    primaryContainer = VermilionContainerLight,
    onPrimaryContainer = OnVermilionContainerLight,
    secondary = DeepTealLight,
    onSecondary = Color.White,
    secondaryContainer = TealContainerLight,
    onSecondaryContainer = OnTealContainerLight,
    tertiary = MutedSlate,
    onTertiary = Color.White,
    background = WarmAlabaster,
    onBackground = DeepInk,
    surface = SoftStone,
    onSurface = DeepInk,
    surfaceVariant = WarmSlateTint,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = CrispIvory,
    surfaceContainerLow = WarmAlabaster,
    surfaceContainer = CrispIvory,
    surfaceContainerHigh = WarmSlateTint,
    surfaceContainerHighest = StoneBorder,
    outline = MutedSlate,
    outlineVariant = StoneBorder,
    error = ErrorRedLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorRedLight,
  )

val CalculatorShapes =
  Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

@Composable
fun CalculatorTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = CalculatorTypography,
    shapes = CalculatorShapes,
    content = content,
  )
}
