package com.example.echojournal.presentation.navigation

import java.net.URLEncoder // <-- You need this import!
import java.nio.charset.StandardCharsets // <-- Recommended for encoding

sealed class Screen(val route: String) {
    object JournalHistory : Screen("journal_history")
    object CreateRecord : Screen("create_record") {
        // FIX: URL-encode the path before appending it to the route
        fun passPath(path: String): String {
            // URLEncoder.encode safely prepares the path for the navigation URL
            val encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8.toString())
            return "create_record/$encodedPath"
        }
    }
}