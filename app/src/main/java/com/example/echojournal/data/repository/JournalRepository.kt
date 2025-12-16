package com.example.echojournal.data.repository

import com.example.echojournal.data.local.dao.JournalEntryDao
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.domain.model.Mood
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val dao: JournalEntryDao) {

    // 1. Get all entries
    val allEntries: Flow<List<JournalEntry>> = dao.getAllEntries()

    /**
     * Filters entries by Mood and Topic.
     * Logic:
     * - We convert the String mood names (from UI) -> Mood Enums (for DB).
     * - We take the first topic selected to filter by (simple search).
     */
    fun getFilteredEntries(moodNames: List<String>, topics: List<String>): Flow<List<JournalEntry>> {
        // Convert UI Strings ("Stressed") back to Domain Enums (Mood.Stressed)
        val moodEnums = moodNames.map { Mood.fromName(it) }

        // Logic: If topics are empty, pass null so the DAO ignores the filter.
        // If there are topics, we just pick the first one for the search query
        // (Room LIKE query usually handles one string pattern at a time cleanly)
        val topicsQuery = if (topics.isNotEmpty()) topics[0] else null

        return dao.getFilteredEntries(
            moods = moodEnums,
            topicsQuery = topicsQuery
        )
    }

    // 2. Insert
    suspend fun insert(entry: JournalEntry) {
        dao.insertEntry(entry)
    }

    // 3. Delete
    suspend fun deleteEntry(entryId: String) {
        dao.deleteEntryById(entryId)
    }
}