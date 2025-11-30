package com.example.echojournal.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.echojournal.ui.home.CreateRecordScreen
import com.example.echojournal.ui.home.HomeScreen
import com.example.echojournal.ui.home.HomeViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel() // This is the single, shared instance
) {
    NavHost(
        navController = navController,
        startDestination = Screen.JournalHistory.route
    ) {

        // Home / Journal History Screen (Correctly passed)
        composable(Screen.JournalHistory.route) {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }

        // Create Record Screen με audioFilePath argument
        composable(
            route = "create_record/{audioFilePath}",
            arguments = listOf(navArgument("audioFilePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val audioPath = backStackEntry.arguments?.getString("audioFilePath") ?: ""
            CreateRecordScreen(
                navController = navController,
                audioFilePath = audioPath,
                viewModel = homeViewModel // FIX: Pass the shared ViewModel explicitly
            )
        }
    }
}