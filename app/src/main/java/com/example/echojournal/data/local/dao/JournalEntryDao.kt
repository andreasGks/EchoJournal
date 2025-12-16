package com.example.echojournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.domain.model.Mood
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    /**
     * Filters entries by Mood and Topics.
     * Note: Room converts the Mood Enum list to Strings using the TypeConverter automatically.
     */
    @Query("""
        SELECT * FROM journal_entries 
        WHERE 
            (:moodsIsEmpty = 1 OR mood IN (:moods)) 
        AND 
            -- Simple string matching for topics since they are stored as "Topic1,Topic2"
            (:topicsQuery IS NULL OR topics LIKE '%' || :topicsQuery || '%')
        ORDER BY timestamp DESC
    """)
    fun getFilteredEntries(
        moods: List<Mood>,
        topicsQuery: String?,
        moodsIsEmpty: Boolean = moods.isEmpty()
    ): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: String)
}