package com.andreasgks.echojournal.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // 1. Helps with full screen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // 2. Required for the theme switch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.andreasgks.echojournal.core.designsystem.EchoJournalTheme
import com.andreasgks.echojournal.data.repository.SettingsRepository
import com.andreasgks.echojournal.presentation.history.HistoryViewModel
import com.andreasgks.echojournal.presentation.navigation.NavGraph
import com.andreasgks.echojournal.presentation.onboarding.OnboardingScreen
import com.andreasgks.echojournal.presentation.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean -> }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // 🔥 FIX 1: This handles the Splash Screen transition
        // It swaps 'Theme.App.SplashScreen' -> 'Theme.EchoJournal.NoActionBar'
        installSplashScreen()

        // 🔥 FIX 2: This forces the content to go behind the status/nav bars
        // This removes black bars at the top/bottom
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        // Request Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            EchoJournalTheme {
                // Since we use the system splash screen now, we *could* remove this custom one,
                // but if you want to keep your animation logic, it is fine to leave it.
                var showSplash by remember { mutableStateOf(true) }
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                val onboardingCompleted by settingsRepository.isOnboardingCompleted.collectAsState(initial = false)
                val historyViewModel: HistoryViewModel = hiltViewModel()

                if (showSplash) {
                    SplashScreen(onNavigateNext = { showSplash = false })
                } else {
                    if (!onboardingCompleted) {
                        OnboardingScreen(
                            onFinish = {
                                scope.launch {
                                    settingsRepository.setOnboardingCompleted()
                                }
                            }
                        )
                    } else {
                        NavGraph(
                            navController = navController,
                            homeViewModel = historyViewModel
                        )
                    }
                }
            }
        }
    }
}