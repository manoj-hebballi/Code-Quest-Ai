package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CodeMintGreen,
    secondary = TechElectricBlue,
    tertiary = CodeYellow,
    background = ObsidianBlack,
    surface = DarkSlateSurface,
    surfaceVariant = MediumSlateCard,
    onPrimary = ObsidianBlack,      // Light text on Accent Purple button
    onSecondary = ObsidianBlack,    // Light text on brand slate button
    onBackground = TextCrispWhite,   // Dark text on Clean background
    onSurface = TextCrispWhite,      // Dark text on white Surface
    onSurfaceVariant = TextLightGrey, // Medium text on pastel surfaceVariant
    outline = LightSlateBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CodeMintGreen,
    secondary = TechElectricBlue,
    tertiary = CodeYellow,
    background = ObsidianBlack,
    surface = DarkSlateSurface,
    surfaceVariant = MediumSlateCard,
    onPrimary = ObsidianBlack,      // Light text on Accent Purple button
    onSecondary = ObsidianBlack,    // Light text on brand slate button
    onBackground = TextCrispWhite,   // Dark text on Clean background
    onSurface = TextCrispWhite,      // Dark text on white Surface
    onSurfaceVariant = TextLightGrey, // Medium text on pastel surfaceVariant
    outline = LightSlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Enforce our custom Obsidian palette for absolute brand clarity!
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
