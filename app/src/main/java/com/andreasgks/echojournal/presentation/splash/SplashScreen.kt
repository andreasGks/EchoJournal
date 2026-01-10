package com.andreasgks.echojournal.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andreasgks.echojournal.R
import com.andreasgks.echojournal.core.designsystem.SplashBackground

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    // Launch splash timer once
    LaunchedEffect(Unit) {
        viewModel.startSplashTimer {
            onNavigateNext()
        }
    }

    // Centered Box with background color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        // App logo or splash image
        Image(
            painter = painterResource(id = R.drawable.mood),
            contentDescription = "Splash Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    // ✅ Preview version — doesn’t trigger the timer or navigation
    SplashScreen(onNavigateNext = {})
}

