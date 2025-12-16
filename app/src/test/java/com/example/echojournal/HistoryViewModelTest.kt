package com.example.echojournal

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.data.repository.JournalRepository
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

    private val mockRepository: JournalRepository = mock()

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val dummyEntries = listOf(
        // FIX: Added the missing 'description' parameter
        JournalEntry(id = "1", title = "A", description = "Test A", audioFilePath = "a", mood = "Happy", tags = listOf("Work")),
        JournalEntry(id = "2", title = "B", description = "Test B", audioFilePath = "b", mood = "Sad", tags = listOf("Family"))
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(mockRepository.allEntries).thenReturn(flowOf(dummyEntries))

        viewModel = HistoryViewModel(mockRepository)
    }

    @Test
    fun toggleMoodFilter_addsMood_whenNotSelected() = runTest {
        viewModel.toggleMoodFilter("Happy")
        val expectedMoods = setOf("Happy")
        assertEquals(expectedMoods, viewModel.selectedMoods.value)
    }

    @Test
    fun toggleMoodFilter_removesMood_whenAlreadySelected() = runTest {
        viewModel.toggleMoodFilter("Happy")
        viewModel.toggleMoodFilter("Happy")
        assertEquals(emptySet<String>(), viewModel.selectedMoods.value)
    }

    @Test
    fun clearMoodFilters_clearsAllSelectedMoods() = runTest {
        viewModel.toggleMoodFilter("Happy")
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
        viewModel.toggleMoodFilter("Happy")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockRepository).getFilteredEntries(
            moods = listOf("Happy"),
            topics = emptyList()
        )
    }
}