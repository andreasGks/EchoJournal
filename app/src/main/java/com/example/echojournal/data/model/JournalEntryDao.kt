package com.example.echojournal.data.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    // Existing Query: Used when ALL entries are needed (no filter applied)
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    // NEW Combined Query: Filters by both moods AND tags (topics).
    // :moodsIsEmpty and :tagsIsEmpty are used to effectively disable the WHERE clause checks if the filter lists are empty.
    @Query("""
        SELECT * FROM journal_entries 
        WHERE (:moodsIsEmpty OR mood IN (:moods)) 
        AND (:tagsIsEmpty OR tags LIKE '%' || REPLACE(:tags, ',', '%') || '%') 
        ORDER BY timestamp DESC
    """)
    fun getFilteredEntries(
        moods: List<String>,
        tags: List<String>,
        // These arguments use default values based on the input lists, allowing the query to efficiently decide whether to apply the filter.
        moodsIsEmpty: Boolean = moods.isEmpty(),
        tagsIsEmpty: Boolean = tags.isEmpty()
    ): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)
}