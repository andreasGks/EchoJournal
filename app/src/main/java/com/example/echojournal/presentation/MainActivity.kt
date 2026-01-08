package com.example.echojournal.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.echojournal.core.designsystem.EchoJournalTheme
import com.example.echojournal.data.repository.SettingsRepository
import com.example.echojournal.presentation.history.HistoryViewModel
import com.example.echojournal.presentation.navigation.NavGraph
import com.example.echojournal.presentation.onboarding.OnboardingScreen
import com.example.echojournal.presentation.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject SettingsRepository to read/write onboarding status
    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean -> }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            EchoJournalTheme {
                var showSplash by remember { mutableStateOf(true) }
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                // Read Onboarding Status
                // "collectAsState" converts the Flow from repository into a Boolean state
                val onboardingCompleted by settingsRepository.isOnboardingCompleted.collectAsState(initial = false)

                val historyViewModel: HistoryViewModel = hiltViewModel()

                if (showSplash) {
                    SplashScreen(onNavigateNext = { showSplash = false })
                } else {
                    // Logic: If Onboarding NOT done -> Show Onboarding. Else -> Show App.
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