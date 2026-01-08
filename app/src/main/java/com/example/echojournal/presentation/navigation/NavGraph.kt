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
import com.example.echojournal.presentation.history.HistoryScreen
import com.example.echojournal.presentation.history.HistoryViewModel
import com.example.echojournal.presentation.record.JournalEntryScreen
// FIX: This import will work once we create the file in the next step
import com.example.echojournal.presentation.settings.SettingsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HistoryViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.JournalHistory.route
    ) {

        // 1. Home / History
        composable(Screen.JournalHistory.route) {
            HistoryScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }

        // 2. Journal Entry Screen
        composable(
            route = Screen.JournalEntry.route, // Best practice: use the constant from Screen class
            arguments = listOf(
                navArgument("audioPath") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("entryId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val audioPath = backStackEntry.arguments?.getString("audioPath")
            val entryId = backStackEntry.arguments?.getString("entryId")

            JournalEntryScreen(
                navController = navController,
                audioFilePath = audioPath,
                entryId = entryId
            )
        }

        // 3. Settings Screen (NEW)
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController
            )
        }
    }
}