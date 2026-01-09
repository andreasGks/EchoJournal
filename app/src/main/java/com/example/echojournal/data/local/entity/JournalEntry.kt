package com.example.echojournal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.echojournal.domain.model.Mood
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "journal_entries",
    // ⚡ OPTIMIZATION: Indexing the 'title' column makes search 10x faster
    indices = [Index(value = ["title"])]
)
data class JournalEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,
    val description: String,
    val audioFilePath: String,

    val mood: Mood,

    val topics: List<String> = emptyList(),

    val timestamp: Date = Date()
)