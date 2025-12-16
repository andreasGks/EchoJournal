package com.example.echojournal.presentation.history

import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.data.repository.JournalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class HistoryViewModel(private val repository: JournalRepository) : ViewModel() {

    // ----------------------------------------------------
    // MOOD Filter
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

    // ----------------------------------------------------
    // TOPIC Filter
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

    // ----------------------------------------------------
    // FILTERED DATA FLOW
    val journalEntries: StateFlow<List<JournalEntry>> =
        combine(_selectedMoods, _selectedTopics) { moods, topics ->
            Pair(moods, topics)
        }.flatMapLatest { (moods, topics) ->
            if (moods.isEmpty() && topics.isEmpty()) {
                repository.allEntries
            } else {
                repository.getFilteredEntries(moods.toList(), topics.toList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ----------------------------------------------------
    // DATA ACTIONS
    fun addEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }

    /**
     * NEW: Function to delete a journal entry by ID, called from the UI (HomeScreen).
     */
    fun deleteJournalEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
            // The journalEntries StateFlow will automatically update because it observes the database.
        }
    }

    // ----------------------------------------------------
    // RECORDING STATE
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _recordDuration = MutableStateFlow(0)
    val recordDuration: StateFlow<Int> = _recordDuration.asStateFlow()

    var recordedFilePath: String? = null
    private var recorder: MediaRecorder? = null
    private var timerJob: Job? = null

    // ----------------------------------------------------
    // RECORDING LOGIC

    fun startRecording(file: File) {
        try {
            stopRecording() // Safety check

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