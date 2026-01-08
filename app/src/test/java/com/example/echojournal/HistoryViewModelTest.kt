package com.example.echojournal

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.data.repository.JournalRepository
import com.example.echojournal.data.repository.SettingsRepository
import com.example.echojournal.domain.model.Mood
import com.example.echojournal.presentation.history.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HistoryViewModel

    // 1. Mock the main Repository
    private val mockRepository: JournalRepository = mock()

    // 2. Mock the NEW Settings Repository
    private val mockSettingsRepository: SettingsRepository = mock()

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    // 3. Update Dummy Data to match your Mood Enum and JournalEntry class
    private val dummyEntries = listOf(
        JournalEntry(
            id = "1",
            title = "A",
            description = "Test A",
            audioFilePath = "a",
            mood = Mood.Excited, // Fixed: Used 'Excited' instead of 'Happy'
            topics = listOf("Work") // Fixed: Used 'topics' instead of 'tags'
        ),
        JournalEntry(
            id = "2",
            title = "B",
            description = "Test B",
            audioFilePath = "b",
            mood = Mood.Sad, // Fixed: Used 'Sad' which exists in your file
            topics = listOf("Family")
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock the flows required by ViewModel init
        whenever(mockRepository.allEntries).thenReturn(flowOf(dummyEntries))

        // Mock Settings flows (ViewModel reads these immediately)
        whenever(mockSettingsRepository.availableTopics).thenReturn(flowOf(emptySet()))
        whenever(mockSettingsRepository.defaultMood).thenReturn(flowOf(null))
        whenever(mockSettingsRepository.defaultTopics).thenReturn(flowOf(emptySet()))

        // 4. Pass BOTH repositories to the constructor
        viewModel = HistoryViewModel(mockRepository, mockSettingsRepository)
    }

    @Test
    fun toggleMoodFilter_addsMood_whenNotSelected() = runTest {
        // Use the String name "Excited" to match the Enum
        viewModel.toggleMoodFilter("Excited")
        val expectedMoods = setOf("Excited")
        assertEquals(expectedMoods, viewModel.selectedMoods.value)
    }

    @Test
    fun toggleMoodFilter_removesMood_whenAlreadySelected() = runTest {
        viewModel.toggleMoodFilter("Excited")
        viewModel.toggleMoodFilter("Excited")
        assertEquals(emptySet<String>(), viewModel.selectedMoods.value)
    }

    @Test
    fun clearMoodFilters_clearsAllSelectedMoods() = runTest {
        viewModel.toggleMoodFilter("Excited")
        viewModel.toggleMoodFilter("Sad")
        viewModel.clearMoodFilters()
        assertEquals(emptySet<String>(), viewModel.selectedMoods.value)
    }

    @Test
    fun journalEntries_emitsAllEntries_whenNoFiltersSelected() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(dummyEntries, viewModel.journalEntries.value)
    }

    @Test
    fun journalEntries_callsFilteredEntries_whenMoodSelected() = runTest {
        viewModel.toggleMoodFilter("Excited")
        testDispatcher.scheduler.advanceUntilIdle()

        // 5. Update verify to match the new Repository function signature
        verify(mockRepository).getFilteredEntries(
            moodNames = listOf("Excited"),
            topics = emptyList(),
            searchQuery = ""
        )
    }
}