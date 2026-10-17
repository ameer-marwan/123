package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    AGRITECH_EMERALD,
    DARK,
    LIGHT
}

private val AgriEmeraldColorScheme = darkColorScheme(
    primary = AgriEmeraldPrimary,
    onPrimary = AgriEmeraldOnPrimary,
    primaryContainer = AgriEmeraldCard,
    onPrimaryContainer = AgriEmeraldPrimary,
    secondary = AgriEmeraldSecondary,
    onSecondary = Color(0xFF003822),
    secondaryContainer = AgriEmeraldCard,
    tertiary = AgriEmeraldAccentAmber,
    background = AgriEmeraldDarkBg,
    surface = AgriEmeraldSurface,
    onBackground = AgriEmeraldTextPrimary,
    onSurface = AgriEmeraldTextPrimary,
    surfaceVariant = AgriEmeraldCard,
    onSurfaceVariant = AgriEmeraldTextSecondary,
    outline = AgriEmeraldCardBorder
)

private val SlateDarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A),
    onPrimary = Color(0xFF00390E),
    secondary = Color(0xFF81C784),
    tertiary = Color(0xFFFFCA28),
    background = SlateDarkBg,
    surface = SlateDarkSurface,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateDarkCard,
    onSurfaceVariant = SlateTextSecondary,
    outline = Color(0xFF334155)
)

private val SunlightLightColorScheme = lightColorScheme(
    primary = SunlightPrimary,
    onPrimary = Color.White,
    secondary = SunlightSecondary,
    onSecondary = Color.White,
    tertiary = Color(0xFFF57F17),
    background = SunlightLightBg,
    surface = SunlightLightSurface,
    onBackground = SunlightTextPrimary,
    onSurface = SunlightTextPrimary,
    surfaceVariant = SunlightLightCard,
    onSurfaceVariant = SunlightTextSecondary,
    outline = Color(0xFFC8D6C9)
)

@Composable
fun FieldShiftTheme(
    themeMode: AppThemeMode = AppThemeMode.AGRITECH_EMERALD,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.AGRITECH_EMERALD -> AgriEmeraldColorScheme
        AppThemeMode.DARK -> SlateDarkColorScheme
        AppThemeMode.LIGHT -> SunlightLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
