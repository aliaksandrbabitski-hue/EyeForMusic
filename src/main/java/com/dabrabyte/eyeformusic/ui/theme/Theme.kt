/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/theme/Theme.kt $
 *
 * Theme of the app
 *
 */

package com.dabrabyte.eyeformusic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = TerraD,
    secondary = TerraGD,
    tertiary = TerraND,
    surfaceContainerHigh = DialogBackD,
)

private val LightColorScheme = lightColorScheme(
    primary = TerraL,
    secondary = TerraGL,
    tertiary = TerraNL
)

// app theme for colors and fonts
@Composable
fun EyeForMusicTheme(
    darkMode: Boolean,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val extendedColors = if (darkMode) darkExtendedColors else lightExtendedColors
    val colorScheme = if (darkMode) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(AppExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object ExtTheme {
    val colors: ExtendedColors
        @Composable
        get() = AppExtendedColors.current
}
