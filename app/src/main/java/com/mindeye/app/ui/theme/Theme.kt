package com.mindeye.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val md_theme_light_primary = Color(0xFF006879)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFA9EDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF001F26)
val md_theme_light_secondary = Color(0xFF4B6268)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCEE7EE)
val md_theme_light_onSecondaryContainer = Color(0xFF061F24)
val md_theme_light_tertiary = Color(0xFF585C80)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFE0E0FF)
val md_theme_light_onTertiaryContainer = Color(0xFF141839)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFAFAFA)
val md_theme_light_onBackground = Color(0xFF191C1D)
val md_theme_light_surface = Color(0xFFFAFAFA)
val md_theme_light_onSurface = Color(0xFF191C1D)
val md_theme_light_surfaceVariant = Color(0xFFDBE4E7)
val md_theme_light_onSurfaceVariant = Color(0xFF3F484B)
val md_theme_light_outline = Color(0xFF6F797B)

val md_theme_dark_primary = Color(0xFF54D7F3)
val md_theme_dark_onPrimary = Color(0xFF003640)
val md_theme_dark_primaryContainer = Color(0xFF004E5C)
val md_theme_dark_onPrimaryContainer = Color(0xFFA9EDFF)
val md_theme_dark_secondary = Color(0xFFB2CBD2)
val md_theme_dark_onSecondary = Color(0xFF1C343A)
val md_theme_dark_secondaryContainer = Color(0xFF334A50)
val md_theme_dark_onSecondaryContainer = Color(0xFFCEE7EE)
val md_theme_dark_tertiary = Color(0xFFC0C4ED)
val md_theme_dark_onTertiary = Color(0xFF2A2E4F)
val md_theme_dark_tertiaryContainer = Color(0xFF404567)
val md_theme_dark_onTertiaryContainer = Color(0xFFE0E0FF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF191C1D)
val md_theme_dark_onBackground = Color(0xFFE1E3E3)
val md_theme_dark_surface = Color(0xFF191C1D)
val md_theme_dark_onSurface = Color(0xFFE1E3E3)
val md_theme_dark_surfaceVariant = Color(0xFF3F484B)
val md_theme_dark_onSurfaceVariant = Color(0xFFBFC8CB)
val md_theme_dark_outline = Color(0xFF899295)

val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline
)

val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline
)

@Composable
fun MindEyeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MindEyeTypography,
        content = content
    )
}
