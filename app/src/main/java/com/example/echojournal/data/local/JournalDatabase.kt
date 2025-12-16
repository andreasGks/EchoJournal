package com.example.echojournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.echojournal.data.local.dao.JournalEntryDao
import com.example.echojournal.data.local.entity.JournalEntry

// FIX: Incremented version to 2 to trigger the schema update
@Database(entities = [JournalEntry::class], version = 2, exportSchema = false)
@TypeConverters(com.example.echojournal.data.local.TypeConverters::class)
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
                )
                    .fallbackToDestructiveMigration() // Wipes old data on version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}