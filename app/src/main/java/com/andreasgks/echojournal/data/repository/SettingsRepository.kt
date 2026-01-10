package com.andreasgks.echojournal.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // 1. Define Unique Keys
    private val KEY_DEFAULT_MOOD = stringPreferencesKey("default_mood")
    private val KEY_DEFAULT_TOPICS = stringSetPreferencesKey("default_topics")
    private val KEY_AVAILABLE_TOPICS = stringSetPreferencesKey("available_topics")
    // NEW: Key to check if onboarding is done
    private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

    // 2. Read Data
    val defaultMood: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_MOOD]
    }

    val defaultTopics: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_TOPICS] ?: emptySet()
    }

    val availableTopics: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_AVAILABLE_TOPICS] ?: emptySet()
    }

    // NEW: Read Onboarding Status (Default is false)
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    // 3. Save Data
    suspend fun saveDefaultMood(moodName: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_MOOD] = moodName
        }
    }

    suspend fun saveDefaultTopics(topics: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_TOPICS] = topics
        }
    }

    suspend fun saveAvailableTopics(topics: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_AVAILABLE_TOPICS] = topics
        }
    }

    suspend fun removeAvailableTopic(topicToRemove: String) {
        dataStore.edit { preferences ->
            val currentTopics = preferences[KEY_AVAILABLE_TOPICS] ?: emptySet()
            if (currentTopics.contains(topicToRemove)) {
                preferences[KEY_AVAILABLE_TOPICS] = currentTopics - topicToRemove
            }
        }
    }

    // NEW: Save that we finished onboarding
    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = true
        }
    }
}