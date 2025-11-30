package com.example.echojournal.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// White splash background
val SplashBackground = Color(0xFFFFFFFF)

// Basic light color scheme
private val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    background = SplashBackground,
    onBackground = Color.Black
)

@Composable
fun EchoJournalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
