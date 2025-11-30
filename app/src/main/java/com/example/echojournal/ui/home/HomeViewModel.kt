package com.example.echojournal.ui.home

import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echojournal.data.model.JournalEntry
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

open class HomeViewModel(private val repository: JournalRepository) : ViewModel() {

    // ----------------------------------------------------
    // MOOD Filter State Management
    private val _selectedMoods = MutableStateFlow(emptySet<String>())
    val selectedMoods: StateFlow<Set<String>> = _selectedMoods.asStateFlow()

    fun toggleMoodFilter(mood: String) {
        viewModelScope.launch {
            _selectedMoods.update { current ->
                if (current.contains(mood)) {
                    current - mood
                } else {
                    current + mood
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
    // TOPIC Filter State Management
    private val _selectedTopics = MutableStateFlow(emptySet<String>())
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    fun toggleTopicFilter(topic: String) {
        viewModelScope.launch {
            _selectedTopics.update { current ->
                if (current.contains(topic)) {
                    current - topic
                } else {
                    current + topic
                }
            }
        }
    }

    fun clearTopicFilters() {
        viewModelScope.launch {
            _selectedTopics.update { emptySet() }
        }
    }

    // ----------------------------------------------------
    // FILTERED DATA FLOW
    val journalEntries: StateFlow<List<JournalEntry>> =
        _selectedMoods.combine(_selectedTopics) { moods, topics ->
            Pair(moods, topics)
        }
            .flatMapLatest { (moods, topics) ->
                if (moods.isEmpty() && topics.isEmpty()) {
                    repository.allEntries
                } else {
                    repository.getFilteredEntries(moods.toList(), topics.toList())
                }
            }
            .stateIn(
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

    // ----------------------------------------------------
    // RECORDING LOGIC (Converted to StateFlow)

    // Using StateFlow instead of mutableStateOf
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _recordDuration = MutableStateFlow(0)
    val recordDuration: StateFlow<Int> = _recordDuration.asStateFlow()

    // Non-UI state variables can remain standard vars
    var recordedFilePath: String? = null
    var isPreview = false

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

            // Update flows
            _isRecording.value = true
            _isPaused.value = false
            _recordDuration.value = 0

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
            while (_isRecording.value) {
                delay(1000)
                if (!_isPaused.value) {
                    _recordDuration.update { it + 1 }
                }
            }
        }
    }

    /**
     * Pauses the current recording (API 24+ only).
     */
    fun pauseRecording() {
        try {
            if (_isRecording.value && !_isPaused.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    recorder?.pause()
                    _isPaused.value = true
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
            if (_isPaused.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    recorder?.resume()
                    _isPaused.value = false
                    // No need to restart timer job explicitly if the while loop is still running,
                    // but usually, it's safer to ensure logic continues.
                    // Based on previous logic, the while loop checks !isPaused.
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
            if (_isRecording.value) {
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

            _isRecording.value = false
            _isPaused.value = false
            timerJob?.cancel()
        }
    }

    /**
     * Reset all states after a recording ends or is cancelled.
     */
    fun resetState() {
        _isSaving.value = false
        _isRecording.value = false
        _isPaused.value = false
        _recordDuration.value = 0
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
}