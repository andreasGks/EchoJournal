package com.andreasgks.echojournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.andreasgks.echojournal.data.local.entity.JournalEntry
import com.andreasgks.echojournal.domain.model.Mood
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    fun getEntryById(entryId: String): Flow<JournalEntry?>

    /**
     * Filters by Mood, Topic, AND Search Query (Title or Description).
     */
    @Query("""
        SELECT * FROM journal_entries 
        WHERE 
            -- Filter by Mood (if list is not empty)
            (:moodsIsEmpty = 1 OR mood IN (:moods)) 
        AND 
            -- Filter by Topic (if selected)
            (:topicsQuery IS NULL OR topics LIKE '%' || :topicsQuery || '%')
        AND
            -- Filter by Search Text (Title OR Description)
            (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        ORDER BY timestamp DESC
    """)
    fun getFilteredEntries(
        moods: List<Mood>,
        topicsQuery: String?,
        searchQuery: String?, // NEW PARAMETER
        moodsIsEmpty: Boolean = moods.isEmpty()
    ): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Update
    suspend fun updateEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: String)
}