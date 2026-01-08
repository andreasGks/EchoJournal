package com.example.echojournal.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {

    // 1. Home / History Screen
    data object JournalHistory : Screen("journal_history")

    // 2. Entry Screen (Handles both Creating and Editing)
    data object JournalEntry : Screen("journal_entry?audioPath={audioPath}&entryId={entryId}") {

        /**
         * Use this when creating a NEW entry from a recording.
         */
        fun create(audioPath: String): String {
            val encodedPath = Uri.encode(audioPath)
            return "journal_entry?audioPath=$encodedPath"
        }

        /**
         * Use this when EDITING an existing entry.
         */
        fun edit(entryId: String): String {
            return "journal_entry?entryId=$entryId"
        }
    }

    // 3. Settings Screen (NEW)
    data object Settings : Screen("settings")
}