package com.example.echojournal.data.repository

import com.example.echojournal.data.local.dao.JournalEntryDao
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.domain.model.Mood
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val dao: JournalEntryDao) {

    // 1. Get all entries
    val allEntries: Flow<List<JournalEntry>> = dao.getAllEntries()

    // 2. Get Entry by ID
    fun getEntryById(entryId: String): Flow<JournalEntry?> {
        return dao.getEntryById(entryId)
    }

    // 3. Filter Logic (Updated with Search)
    fun getFilteredEntries(
        moodNames: List<String>,
        topics: List<String>,
        searchQuery: String? = null // NEW PARAMETER
    ): Flow<List<JournalEntry>> {
        val moodEnums = moodNames.map { Mood.fromName(it) }

        // Pick the first topic if available (Room LIKE limitation simplification)
        val topicsQuery = if (topics.isNotEmpty()) topics[0] else null

        // Clean search query: if blank, pass null
        val cleanSearchQuery = if (searchQuery.isNullOrBlank()) null else searchQuery

        return dao.getFilteredEntries(
            moods = moodEnums,
            topicsQuery = topicsQuery,
            searchQuery = cleanSearchQuery
        )
    }

    // 4. Insert
    suspend fun insert(entry: JournalEntry) {
        dao.insertEntry(entry)
    }

    // 5. Update
    suspend fun update(entry: JournalEntry) {
        dao.updateEntry(entry)
    }

    // 6. Delete
    suspend fun deleteEntry(entryId: String) {
        dao.deleteEntryById(entryId)
    }
}