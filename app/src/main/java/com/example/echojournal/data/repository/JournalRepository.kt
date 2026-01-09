package com.example.echojournal.data.repository

import com.example.echojournal.data.local.dao.JournalEntryDao
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.domain.model.Mood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class JournalRepository(private val dao: JournalEntryDao) {

    // 1. Get all entries
    val allEntries: Flow<List<JournalEntry>> = dao.getAllEntries()
        .flowOn(Dispatchers.IO) // Ensure stream flows on IO thread

    // 2. Get Entry by ID
    fun getEntryById(entryId: String): Flow<JournalEntry?> {
        return dao.getEntryById(entryId)
            .flowOn(Dispatchers.IO)
    }

    // 3. Filter Logic
    fun getFilteredEntries(
        moodNames: List<String>,
        topics: List<String>,
        searchQuery: String? = null
    ): Flow<List<JournalEntry>> {
        val moodEnums = moodNames.map { Mood.fromName(it) }
        val topicsQuery = if (topics.isNotEmpty()) topics[0] else null
        val cleanSearchQuery = if (searchQuery.isNullOrBlank()) null else searchQuery

        return dao.getFilteredEntries(
            moods = moodEnums,
            topicsQuery = topicsQuery,
            searchQuery = cleanSearchQuery
        ).flowOn(Dispatchers.IO) // ⚡ OPTIMIZATION: Search calc on IO thread
    }

    // 4. Insert
    suspend fun insert(entry: JournalEntry) {
        // ⚡ OPTIMIZATION: Force execution on background thread
        withContext(Dispatchers.IO) {
            dao.insertEntry(entry)
        }
    }

    // 5. Update
    suspend fun update(entry: JournalEntry) {
        withContext(Dispatchers.IO) {
            dao.updateEntry(entry)
        }
    }

    // 6. Delete
    suspend fun deleteEntry(entryId: String) {
        withContext(Dispatchers.IO) {
            dao.deleteEntryById(entryId)
        }
    }
}