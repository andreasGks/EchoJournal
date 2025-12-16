package com.example.echojournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.echojournal.domain.model.Mood
import java.util.Date
import java.util.UUID

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,
    val description: String,
    val audioFilePath: String, // Critical for the recording feature

    // We use the Enum directly. Our TypeConverter will handle saving it as a String.
    val mood: Mood,

    // Requirements call this "Topics"
    val topics: List<String> = emptyList(),

    val timestamp: Date = Date()
)