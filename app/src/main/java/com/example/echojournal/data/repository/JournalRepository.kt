package com.example.echojournal.data.repository

import com.example.echojournal.data.model.JournalEntry
import com.example.echojournal.data.model.JournalEntryDao
import kotlinx.coroutines.flow.Flow

// Repository acts as a clean API for the ViewModel to access data
class JournalRepository(private val dao: JournalEntryDao) {

    // 1. Existing: Expose the Flow directly from the DAO for ALL entries
    val allEntries: Flow<List<JournalEntry>> = dao.getAllEntries()

    // NEW: Combined Method for filtered query (Moods AND Topics/Tags)
    fun getFilteredEntries(moods: List<String>, tags: List<String>): Flow<List<JournalEntry>> {
        return dao.getFilteredEntries(moods, tags)
    }

    // 2. Insert method (run in ViewModel's scope)
    suspend fun insert(entry: JournalEntry) {
        dao.insertEntry(entry)
    }

    // 3. Delete method (run in ViewModel's scope)
    suspend fun delete(entry: JournalEntry) {
        dao.deleteEntry(entry)
    }
}