package com.fyrefly.fireflycollege.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NightColors = darkColorScheme(
    primary = GlowGreen,
    onPrimary = FireflyInk,
    primaryContainer = ForestContainer,
    onPrimaryContainer = MintContainer,
    secondary = MistCyan,
    onSecondary = CyanInk,
    secondaryContainer = CyanContainerDeep,
    onSecondaryContainer = CyanContainer,
    tertiary = LanternAmber,
    onTertiary = AmberInk,
    tertiaryContainer = AmberContainerDeep,
    onTertiaryContainer = AmberContainer,
    background = NightNavy,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightMutedText,
    outline = NightOutline,
    error = ErrorDark,
    onError = ErrorInkDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorContainerInkDark
)

private val DayColors = lightColorScheme(
    primary = GlowGreenDeep,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = FireflyInk,
    secondary = MistCyanDeep,
    onSecondary = Color.White,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanInk,
    tertiary = LanternAmberDeep,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberInk,
    background = DayPaper,
    onBackground = DayText,
    surface = DaySurface,
    onSurface = DayText,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = DayMutedText,
    outline = DayOutline,
    error = ErrorLight,
    onError = ErrorInkLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorContainerInkLight
)

@Composable
fun FireflyCollegeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) NightColors else DayColors,
        typography = FireflyTypography,
        content = content
    )
}
