package com.example.echojournal.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.echojournal.presentation.record.CreateRecordScreen
// FIX 1: Import HistoryScreen instead of HomeScreen
import com.example.echojournal.presentation.history.HistoryScreen
import com.example.echojournal.presentation.history.HistoryViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HistoryViewModel = viewModel() // This is the single, shared instance
) {
    NavHost(
        navController = navController,
        startDestination = Screen.JournalHistory.route
    ) {

        // Home / Journal History Screen
        composable(Screen.JournalHistory.route) {
            // FIX 2: Call HistoryScreen instead of HomeScreen
            HistoryScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }

        // Create Record Screen with audioFilePath argument
        composable(
            route = "create_record/{audioFilePath}",
            arguments = listOf(navArgument("audioFilePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val audioPath = backStackEntry.arguments?.getString("audioFilePath") ?: ""
            CreateRecordScreen(
                navController = navController,
                audioFilePath = audioPath,
                viewModel = homeViewModel
            )
        }
    }
}