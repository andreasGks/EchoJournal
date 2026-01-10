package com.andreasgks.echojournal.presentation.onboarding

import androidx.annotation.DrawableRes

// A simple data class to hold slide information
data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int // Use annotation to ensure it's a valid Drawable
)