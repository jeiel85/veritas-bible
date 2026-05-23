package com.veritasbible.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = Parchment,
    primaryContainer = NavyContainer,
    onPrimaryContainer = DeepInk,
    secondary = Gold,
    onSecondary = DeepNavy,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = DeepInk,
    tertiary = IndigoSoft,
    onTertiary = Parchment,
    background = LightBackground,
    onBackground = OnLight,
    surface = LightSurface,
    onSurface = OnLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightVariant,
    outline = OnLightVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoBright,
    onPrimary = DeepInk,
    primaryContainer = DeepNavy,
    onPrimaryContainer = NavyContainer,
    secondary = Gold,
    onSecondary = DeepInk,
    secondaryContainer = InkBlue,
    onSecondaryContainer = GoldSoft,
    tertiary = IndigoSoft,
    onTertiary = DeepInk,
    background = DarkBackground,
    onBackground = OnDark,
    surface = DarkSurface,
    onSurface = OnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkVariant,
    outline = OnDarkVariant,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
