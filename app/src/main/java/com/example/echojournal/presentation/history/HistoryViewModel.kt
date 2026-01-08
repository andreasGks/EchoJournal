package com.example.echojournal.presentation.history

import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.data.repository.JournalRepository
import com.example.echojournal.data.repository.SettingsRepository
import com.example.echojournal.domain.model.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: JournalRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // ----------------------------------------------------
    // SETTINGS / DEFAULTS
    // ----------------------------------------------------
    suspend fun getDefaultMood(): Mood? {
        val name = settingsRepository.defaultMood.firstOrNull()
        return name?.let { Mood.fromName(it) }
    }

    suspend fun getDefaultTopics(): List<String> {
        return settingsRepository.defaultTopics.firstOrNull()?.toList() ?: emptyList()
    }

    // ----------------------------------------------------
    // DYNAMIC TAGS LIST (History + Settings) 🌟
    // ----------------------------------------------------
    // Combines tags from History + Settings, sorts them Alphabetically
    val availableTopics: StateFlow<List<String>> = combine(
        repository.allEntries.map { list -> list.flatMap { it.topics }.toSet() },
        settingsRepository.availableTopics
    ) { historyTags, settingsTags ->
        (historyTags + settingsTags).sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // NEW: Delete a tag permanently from suggestions
    fun deleteTagFromSettings(tag: String) {
        viewModelScope.launch {
            settingsRepository.removeAvailableTopic(tag)
        }
    }

    // ----------------------------------------------------
    // FILTER LOGIC
    // ----------------------------------------------------
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private val _selectedMoods = MutableStateFlow(emptySet<String>())
    val selectedMoods: StateFlow<Set<String>> = _selectedMoods.asStateFlow()

    fun toggleMoodFilter(mood: String) {
        _selectedMoods.update { current ->
            if (current.contains(mood)) current - mood else current + mood
        }
    }

    fun clearMoodFilters() {
        _selectedMoods.update { emptySet() }
    }

    private val _selectedTopics = MutableStateFlow(emptySet<String>())
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    fun toggleTopicFilter(topic: String) {
        _selectedTopics.update { current ->
            if (current.contains(topic)) current - topic else current + topic
        }
    }

    fun clearTopicFilters() {
        _selectedTopics.update { emptySet() }
    }

    val journalEntries: StateFlow<List<JournalEntry>> =
        combine(_selectedMoods, _selectedTopics, _searchQuery) { moods, topics, query ->
            Triple(moods, topics, query)
        }.flatMapLatest { (moods, topics, query) ->
            if (moods.isEmpty() && topics.isEmpty() && query.isBlank()) {
                repository.allEntries
            } else {
                repository.getFilteredEntries(
                    moodNames = moods.toList(),
                    topics = topics.toList(),
                    searchQuery = query
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ----------------------------------------------------
    // DATA ACTIONS (CRUD)
    // ----------------------------------------------------

    // Helper to save new tags to permanent settings
    private suspend fun saveNewTagsToSettings(entryTopics: List<String>) {
        if (entryTopics.isEmpty()) return

        val currentSettingsTags = settingsRepository.availableTopics.first()
        val updatedTags = currentSettingsTags + entryTopics

        if (updatedTags.size > currentSettingsTags.size) {
            settingsRepository.saveAvailableTopics(updatedTags)
        }
    }

    fun addEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.insert(entry)
            saveNewTagsToSettings(entry.topics)
        }
    }

    fun getEntryById(entryId: String): Flow<JournalEntry?> {
        return repository.getEntryById(entryId)
    }

    fun updateEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.update(entry)
            saveNewTagsToSettings(entry.topics)
        }
    }

    fun deleteJournalEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
        }
    }

    // ----------------------------------------------------
    // RECORDING LOGIC
    // ----------------------------------------------------
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _recordDuration = MutableStateFlow(0)
    val recordDuration: StateFlow<Int> = _recordDuration.asStateFlow()

    var recordedFilePath: String? = null
    private var recorder: MediaRecorder? = null
    private var timerJob: Job? = null

    fun startRecording(file: File) {
        try {
            stopRecording()
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recordedFilePath = file.absolutePath
            _isRecording.value = true
            _isPaused.value = false
            _recordDuration.value = 0
            startTimer()
        } catch (e: Exception) {
            e.printStackTrace()
            resetState()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                if (!_isPaused.value) {
                    _recordDuration.update { it + 1 }
                }
            }
        }
    }

    fun pauseRecording() {
        if (_isRecording.value && !_isPaused.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    recorder?.pause()
                    _isPaused.value = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun resumeRecording() {
        if (_isPaused.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    recorder?.resume()
                    _isPaused.value = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopRecording() {
        if (_isRecording.value) {
            try {
                recorder?.stop()
            } catch (e: RuntimeException) {
                recordedFilePath?.let { File(it).delete() }
            } finally {
                recorder?.release()
                recorder = null
                resetState()
            }
        }
    }

    private fun resetState() {
        _isRecording.value = false
        _isPaused.value = false
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        recorder?.release()
        recorder = null
    }
}