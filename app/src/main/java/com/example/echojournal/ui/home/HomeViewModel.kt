package com.example.echojournal.ui.home

import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echojournal.data.model.JournalEntry
import com.example.echojournal.data.repository.JournalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine // NEW: Import combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

// 1. CONSTRUCTOR INJECTION: The ViewModel now REQUIRES a Repository instance.
open class HomeViewModel(private val repository: JournalRepository) : ViewModel() {

    // ----------------------------------------------------
    // MOOD Filter State Management
    private val _selectedMoods = MutableStateFlow(emptySet<String>())
    val selectedMoods: StateFlow<Set<String>> = _selectedMoods.asStateFlow()

    fun toggleMoodFilter(mood: String) {
        viewModelScope.launch {
            _selectedMoods.update { current ->
                if (current.contains(mood)) {
                    current - mood // Remove mood
                } else {
                    current + mood // Add mood
                }
            }
        }
    }

    fun clearMoodFilters() {
        viewModelScope.launch {
            _selectedMoods.update { emptySet() }
        }
    }

    // ----------------------------------------------------
    // NEW: Topic Filter State Management (RESOLVED: This was missing and caused Unresolved reference errors)
    private val _selectedTopics = MutableStateFlow(emptySet<String>())

    // NEW: Expose the selected topics
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    // NEW: Toggle Topic filter
    fun toggleTopicFilter(topic: String) {
        viewModelScope.launch {
            _selectedTopics.update { current ->
                if (current.contains(topic)) {
                    current - topic // Remove topic
                } else {
                    current + topic // Add topic
                }
            }
        }
    }

    // NEW: Function to clear all active topic filters
    fun clearTopicFilters() {
        viewModelScope.launch {
            _selectedTopics.update { emptySet() }
        }
    }
    // ----------------------------------------------------


    // 2. FILTERED DATA FLOW: Combine the two filter states with the database query
    val journalEntries: StateFlow<List<JournalEntry>> =
        // Combine both flows into a single flow of Pair<Set<String>, Set<String>>
        _selectedMoods.combine(_selectedTopics) { moods, topics ->
            Pair(moods, topics)
        }
            // Use flatMapLatest to switch to the new database query flow whenever filters change
            .flatMapLatest { (moods, topics) ->
                if (moods.isEmpty() && topics.isEmpty()) {
                    repository.allEntries // Uses the original getAllEntries query
                } else {
                    // Calls the repository method that accepts both moods and tags
                    repository.getFilteredEntries(moods.toList(), topics.toList())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 3. DATA ACCESS: Refactor addEntry to call the Repository's insert method.
    fun addEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }

    // --- (EXISTING RECORDING LOGIC BELOW REMAINS THE SAME) ---

    var isSaving = mutableStateOf(false)
    var isRecording = mutableStateOf(false)
    var isPaused = mutableStateOf(false)
    var recordedFilePath: String? = null
    var isPreview = false // skip logic during Compose preview
    var recordDuration = mutableStateOf(0) // in seconds

    private var recorder: MediaRecorder? = null
    private var timerJob: Job? = null

    /**
     * Start audio recording to a specified file.
     */
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
            isRecording.value = true
            isPaused.value = false
            recordDuration.value = 0
            startTimer()

        } catch (e: Exception) {
            e.printStackTrace()
            resetState()
        }
    }

    /**
     * Increments the timer while recording is active.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isRecording.value) {
                delay(1000)
                if (!isPaused.value) {
                    recordDuration.value++
                }
            }
        }
    }

    /**
     * Pauses the current recording (API 24+ only).
     */
    fun pauseRecording() {
        try {
            if (isRecording.value && !isPaused.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    recorder?.pause()
                    isPaused.value = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Resumes a paused recording.
     */
    fun resumeRecording() {
        try {
            if (isPaused.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    recorder?.resume()
                    isPaused.value = false
                    startTimer()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Stops recording safely and releases resources.
     */
    fun stopRecording() {
        try {
            if (isRecording.value) {
                recorder?.apply {
                    try {
                        stop()
                    } catch (stopException: RuntimeException) {
                        recordedFilePath?.let { path ->
                            File(path).delete()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder?.release()
            recorder = null
            isRecording.value = false
            isPaused.value = false
            timerJob?.cancel()
        }
    }

    /**
     * Reset all states after a recording ends or is cancelled.
     */
    fun resetState() {
        isSaving.value = false
        isRecording.value = false
        isPaused.value = false
        recordDuration.value = 0
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
}