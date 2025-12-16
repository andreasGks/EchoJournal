package com.example.echojournal.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.echojournal.data.local.JournalDatabase
import com.example.echojournal.data.repository.JournalRepository
import com.example.echojournal.presentation.history.HistoryViewModel

// This factory creates the HistoryViewModel, injecting the required dependencies
class JournalViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    // Lazy initialization ensures the database is built only when needed
    private val database by lazy { JournalDatabase.getDatabase(application) }
    private val repository by lazy { JournalRepository(database.journalEntryDao()) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}