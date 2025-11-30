package com.example.echojournal

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.echojournal.data.JournalDatabase
import com.example.echojournal.data.repository.JournalRepository
import com.example.echojournal.ui.home.HomeViewModel

// This factory creates the HomeViewModel, injecting the required dependencies
class JournalViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    // Lazy initialization of the DAO and Repository to ensure the database is built first
    private val database by lazy { JournalDatabase.getDatabase(application) }
    private val repository by lazy { JournalRepository(database.journalEntryDao()) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}