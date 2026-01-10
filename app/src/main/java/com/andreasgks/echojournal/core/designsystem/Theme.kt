package com.andreasgks.echojournal.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define the Color Scheme using the colors we just created
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    background = SplashBackground,
    surface = SurfaceBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun EchoJournalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        // typography = Typography, // We will add this later if needed
        content = content
    )
}