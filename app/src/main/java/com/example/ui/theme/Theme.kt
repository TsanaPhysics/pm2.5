package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BentoPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    tertiary = WarningAmber,
    background = DarkBg,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Slate400,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoPrimaryLight,
    onPrimaryContainer = BentoPrimaryDark,
    secondary = Slate700,
    onSecondary = Color.White,
    tertiary = AqiUnhealthySensitive,
    background = BentoBg,
    onBackground = Slate900,
    surface = BentoSurface,
    onSurface = Slate900,
    surfaceVariant = BentoSurfaceVariant,
    onSurfaceVariant = Slate500,
    outline = BentoBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
