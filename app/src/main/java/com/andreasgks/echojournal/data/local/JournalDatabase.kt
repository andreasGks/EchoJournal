package com.andreasgks.echojournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andreasgks.echojournal.data.local.dao.JournalEntryDao
import com.andreasgks.echojournal.data.local.entity.JournalEntry

// ⚡ FIX: Version updated to 3 to force a clean rebuild of the database
@Database(entities = [JournalEntry::class], version = 3, exportSchema = false)
@TypeConverters(com.andreasgks.echojournal.data.local.TypeConverters::class)
abstract class JournalDatabase : RoomDatabase() {

    abstract fun journalEntryDao(): JournalEntryDao

    // NOTE: This companion object is mostly ignored because we use Hilt (AppModule),
    // but we keep it updated just in case.
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}