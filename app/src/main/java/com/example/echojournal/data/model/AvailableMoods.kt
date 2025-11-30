package com.example.echojournal.data.model

import com.example.echojournal.R // Import your project's R file

// Define the moods and their associated resource IDs
data class MoodItem(val name: String, val iconResId: Int)

object AvailableMoods {
    val list = listOf(
        MoodItem("Excited", R.drawable.ic_mood_excited),
        MoodItem("Peaceful", R.drawable.ic_mood_peaceful),
        MoodItem("Neutral", R.drawable.ic_mood_neutral),
        MoodItem("Sad", R.drawable.ic_mood_sad),
        MoodItem("Stressed", R.drawable.ic_mood_stressed)
    )
    val nameList = list.map { it.name }

    // Helper map to quickly get the ID by name
    val map = list.associateBy { it.name }
}