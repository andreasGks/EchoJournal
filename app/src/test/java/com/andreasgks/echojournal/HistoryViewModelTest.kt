package com.andreasgks.echojournal

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.andreasgks.echojournal.data.local.entity.JournalEntry
import com.andreasgks.echojournal.data.repository.JournalRepository
import com.andreasgks.echojournal.data.repository.SettingsRepository
import com.andreasgks.echojournal.domain.model.Mood
import com.andreasgks.echojournal.presentation.history.HistoryViewModel
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


    // --- NEW TESTS FOR V1.0 FEATURES ---

    @Test
    fun onSearchQueryChange_updatesValue_and_triggersSearch() = runTest {
        // 1. Simulate typing "Gym"
        viewModel.onSearchQueryChange("Gym")

        // 2. Check if state updated locally
        assertEquals("Gym", viewModel.searchQuery.value)

        // 3. Let coroutines finish
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify Repository was called with the search query
        // Note: We use org.mockito.kotlin.any() for lists because we don't care about filters here
        verify(mockRepository).getFilteredEntries(
            moodNames = org.mockito.kotlin.any(),
            topics = org.mockito.kotlin.any(),
            searchQuery = org.mockito.kotlin.eq("Gym") // Checking specifically for this
        )
    }

    @Test
    fun deleteJournalEntry_callsRepositoryDelete() = runTest {
        // 1. Call delete
        val entryIdToDelete = "1"
        viewModel.deleteJournalEntry(entryIdToDelete)

        // 2. WAIT for the coroutine to finish (Missing Step)
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Verify repository delete function was triggered
        verify(mockRepository).deleteEntry(entryIdToDelete)
    }

    @Test
    fun toggleTopicFilter_addsAndRemovesTopic() = runTest {
        // 1. Select "Work"
        viewModel.toggleTopicFilter("Work")
        assertEquals(setOf("Work"), viewModel.selectedTopics.value)

        // 2. Select "Work" again (should remove it)
        viewModel.toggleTopicFilter("Work")
        assertEquals(emptySet<String>(), viewModel.selectedTopics.value)
    }


}