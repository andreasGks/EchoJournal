package com.andreasgks.echojournal.data.local

import androidx.room.TypeConverter
import com.andreasgks.echojournal.domain.model.Mood
import java.util.Date

/**
 * Handles converting complex types (Date, List, Mood) into types
 * that Room can understand (Long, String).
 */
class TypeConverters {

    // --- Date Converters ---
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // --- Mood Converters (Enum <-> String) ---
    @TypeConverter
    fun fromMood(mood: Mood): String {
        return mood.name // Saves "Stressed" to DB
    }

    @TypeConverter
    fun toMood(value: String): Mood {
        return Mood.fromName(value) // converts "Stressed" back to Mood.Stressed
    }

    // --- List<String> Converters (For Topics) ---
    // Converts List("Family", "Work") -> String("Family,Work")
    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    // Converts String("Family,Work") -> List("Family", "Work")
    @TypeConverter
    fun toList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").map { it.trim() }
    }
}