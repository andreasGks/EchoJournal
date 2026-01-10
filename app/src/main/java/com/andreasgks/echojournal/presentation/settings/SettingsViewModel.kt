package com.andreasgks.echojournal.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreasgks.echojournal.data.repository.SettingsRepository
import com.andreasgks.echojournal.domain.model.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- DEFAULT MOOD ---
    private val _defaultMood = MutableStateFlow<Mood?>(null)
    val defaultMood: StateFlow<Mood?> = _defaultMood.asStateFlow()

    fun setDefaultMood(mood: Mood) {
        _defaultMood.value = mood
        viewModelScope.launch {
            settingsRepository.saveDefaultMood(mood.name)
        }
    }

    // --- DEFAULT TOPICS (Selected) ---
    private val _defaultTopics = MutableStateFlow<Set<String>>(emptySet())
    val defaultTopics: StateFlow<Set<String>> = _defaultTopics.asStateFlow()

    fun toggleDefaultTopic(topic: String) {
        val current = _defaultTopics.value
        val newSet = if (current.contains(topic)) current - topic else current + topic

        _defaultTopics.value = newSet
        viewModelScope.launch {
            settingsRepository.saveDefaultTopics(newSet)
        }
    }

    // --- AVAILABLE TOPICS (All Tags) ---
    private val _availableTopics = MutableStateFlow<Set<String>>(emptySet())
    val availableTopics: StateFlow<Set<String>> = _availableTopics.asStateFlow()

    fun createNewTopic(newTopic: String) {
        if (newTopic.isNotBlank()) {
            val trimmed = newTopic.trim()
            val currentList = _availableTopics.value

            // Only update if it's new
            if (!currentList.contains(trimmed)) {
                val newSet = currentList + trimmed
                _availableTopics.value = newSet

                viewModelScope.launch {
                    settingsRepository.saveAvailableTopics(newSet)
                }
            }

            // Automatically select the new topic as a default
            toggleDefaultTopic(trimmed)
        }
    }

    // NEW: Delete a topic (Available & Default)
    fun deleteTopic(topic: String) {
        // 1. Remove from Available List
        val currentAvailable = _availableTopics.value
        if (currentAvailable.contains(topic)) {
            val newAvailable = currentAvailable - topic
            _availableTopics.value = newAvailable
            viewModelScope.launch {
                settingsRepository.saveAvailableTopics(newAvailable)
            }
        }

        // 2. Remove from Default Selection (if it was selected)
        val currentDefaults = _defaultTopics.value
        if (currentDefaults.contains(topic)) {
            val newDefaults = currentDefaults - topic
            _defaultTopics.value = newDefaults
            viewModelScope.launch {
                settingsRepository.saveDefaultTopics(newDefaults)
            }
        }
    }

    // --- INIT: LOAD SAVED DATA ---
    init {
        // 1. Load Mood
        viewModelScope.launch {
            settingsRepository.defaultMood.collectLatest { moodName ->
                if (moodName != null) {
                    val matchedMood = Mood.entries.find { it.name == moodName }
                    if (matchedMood != null) {
                        _defaultMood.value = matchedMood
                    }
                }
            }
        }

        // 2. Load Selected Default Topics
        viewModelScope.launch {
            settingsRepository.defaultTopics.collectLatest { savedTopics ->
                _defaultTopics.value = savedTopics
            }
        }

        // 3. Load Available Topics (The List)
        viewModelScope.launch {
            // First, get the saved list from DataStore
            val savedList = settingsRepository.availableTopics.first()

            if (savedList.isEmpty()) {
                // If empty (first run), initialize with Default tags
                val initialTags = setOf("Work", "Family", "Health", "Love", "Friends")
                _availableTopics.value = initialTags
                settingsRepository.saveAvailableTopics(initialTags)
            } else {
                // Otherwise, use the saved list
                _availableTopics.value = savedList
            }

            // Continue listening for updates
            settingsRepository.availableTopics.collectLatest { updatedList ->
                if (updatedList.isNotEmpty()) {
                    _availableTopics.value = updatedList
                }
            }
        }
    }
}