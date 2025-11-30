package com.example.echojournal.data.model

import androidx.room.Entity // REQUIRED: For defining the table
import androidx.room.PrimaryKey // REQUIRED: For defining the primary key
import androidx.room.TypeConverters // REQUIRED: For using custom converters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// 1. Define the table name for Room
@Entity(tableName = "journal_entries")
// 2. Reference the TypeConverters class you defined (TypeConverters.kt)
@TypeConverters(DateConverter::class, ListConverter::class)
data class JournalEntry(
    // 3. Define a Primary Key for the table
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,
    val description: String,
    val audioFilePath: String,
    val mood: String?,
    val tags: List<String>,
    // The Date object is now handled by the TypeConverter
    val timestamp: Date = Date()
) {
    // Helper to format the time for display (e.g., 17:30)
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
}