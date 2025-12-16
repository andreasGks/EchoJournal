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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.echojournal.core.designsystem.EchoJournalTheme
import com.example.echojournal.presentation.history.HistoryViewModel
import com.example.echojournal.presentation.navigation.NavGraph
import com.example.echojournal.presentation.splash.SplashScreen

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean -> }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            EchoJournalTheme {
                var showSplash by remember { mutableStateOf(true) }
                val navController = rememberNavController()

                // 1. Instantiate the custom ViewModel Factory using the application context
                val factory = remember { JournalViewModelFactory(application) }

                // 2. Use the factory to create the HistoryViewModel
                val historyViewModel: HistoryViewModel = viewModel(factory = factory)

                if (showSplash) {
                    SplashScreen(onNavigateNext = { showSplash = false })
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