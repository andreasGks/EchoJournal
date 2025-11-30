package com.example.echojournal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.echojournal.data.model.DateConverter
import com.example.echojournal.data.model.JournalEntry
import com.example.echojournal.data.model.JournalEntryDao
import com.example.echojournal.data.model.ListConverter

@Database(entities = [JournalEntry::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, ListConverter::class) // Use both converters
abstract class JournalDatabase : RoomDatabase() {

    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        @Volatile
        private var INSTANCE: JournalDatabase? = null

        fun getDatabase(context: Context): JournalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JournalDatabase::class.java,
                    "journal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}