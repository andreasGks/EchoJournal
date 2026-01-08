package com.example.echojournal.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.echojournal.data.local.JournalDatabase
import com.example.echojournal.data.local.dao.JournalEntryDao
import com.example.echojournal.data.repository.JournalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// NEW: Define the DataStore extension property
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 1. Create the Database
    @Provides
    @Singleton
    fun provideJournalDatabase(app: Application): JournalDatabase {
        return Room.databaseBuilder(
            app,
            JournalDatabase::class.java,
            "journal_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // 2. Provide the DAO
    @Provides
    @Singleton
    fun provideJournalEntryDao(db: JournalDatabase): JournalEntryDao {
        return db.journalEntryDao()
    }

    // 3. Provide the Journal Repository
    @Provides
    @Singleton
    fun provideJournalRepository(dao: JournalEntryDao): JournalRepository {
        return JournalRepository(dao)
    }

    // 4. Provide DataStore (NEW) 💾
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}