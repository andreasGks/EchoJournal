package com.example.echojournal.domain.model

import androidx.compose.ui.graphics.Color
import com.example.echojournal.R
import com.example.echojournal.core.designsystem.*

enum class Mood(
    val iconRes: Int,
    val color: Color,
    val containerColor: Color // The light background
) {
    Stressed(
        iconRes = R.drawable.ic_mood_stressed,
        color = MoodStressed, // References core/designsystem/Color.kt
        containerColor = Color(0xFFFCEBEB)
    ),
    Sad(
        iconRes = R.drawable.ic_mood_sad,
        color = MoodSad,
        containerColor = Color(0xFFEBF5FC)
    ),
    Neutral(
        iconRes = R.drawable.ic_mood_neutral,
        color = MoodNeutral,
        containerColor = Color(0xFFEEF7F3)
    ),
    Peaceful(
        iconRes = R.drawable.ic_mood_peaceful,
        color = MoodPeaceful,
        containerColor = Color(0xFFF8EBF5)
    ),
    Excited(
        iconRes = R.drawable.ic_mood_excited,
        color = MoodExcited,
        containerColor = Color(0xFFFDF0E7)
    );

    // Helper to find a Mood by name (useful for Database Converters)
    companion object {
        fun fromName(name: String?): Mood {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: Neutral
        }
    }
}