package com.example.echojournal.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.echojournal.R
import com.example.echojournal.data.model.JournalEntry
import com.example.echojournal.data.repository.JournalRepository
import com.example.echojournal.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.Flow
import android.media.MediaPlayer // Import MediaPlayer
import com.example.echojournal.data.model.JournalEntryDao // Import DAO for Preview

// The list of all moods for the filtering UI (NOTE: This should ideally be in data/model/Moods.kt)
val allAvailableMoods = listOf("Excited", "Peaceful", "Neutral", "Sad", "Stressed")
// NEW: Mock list of available topics/hashtags
val allAvailableTopics = listOf("Work", "Family", "Health", "Study", "Travel", "Random")


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    var showRecordingSheet by remember { mutableStateOf(false) }
    var showMoodFilterSheet by remember { mutableStateOf(false) }
    var showTopicFilterSheet by remember { mutableStateOf(false) } // NEW: State for Topic filter modal

    // Collect the current filter states and the list of entries
    val currentMoods by viewModel.selectedMoods.collectAsState()
    val currentTopics by viewModel.selectedTopics.collectAsState() // RESOLVED: Used the correct ViewModel state
    val entries: List<JournalEntry> by viewModel.journalEntries.collectAsState()
    val showEmptyState = entries.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(72.dp)
                .background(Color.White)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Your Echo Journal",
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )

            // Mood and Topic Filters
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // --- MOOD FILTER BUTTON ---
                    MoodFilterButton(
                        currentMoods = currentMoods,
                        onClick = { showMoodFilterSheet = true },
                        onClear = { viewModel.clearMoodFilters() }
                    )
                    // --- TOPICS FILTER BUTTON (NEW) ---
                    TopicFilterButton(
                        currentTopics = currentTopics,
                        onClick = { showTopicFilterSheet = true },
                        onClear = { viewModel.clearTopicFilters() } // RESOLVED: Used the correct ViewModel method
                    )
                }
            }


            Image(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Settings Icon",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
            )
        }

        // --- DYNAMIC CONTENT: LIST or EMPTY STATE ---

        if (showEmptyState) {
            // Center icon + text (Original "No Entries" block)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_no_entries),
                    contentDescription = "No Entries Icon",
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No Entries.",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Start recording your first Echo",
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }
        } else {
            // Display the list of entries
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp), // Adjust padding to clear header
                contentPadding = PaddingValues(bottom = 100.dp) // Space for FAB
            ) {
                // Display "TODAY" header
                item {
                    Text(
                        text = "TODAY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // FIX: items now correctly takes the List and the key uses 'entry.id'
                items(entries, key = { entry -> entry.id }) { entry ->
                    JournalEntryCard(entry = entry)
                }
            }
        }


        // Floating button (Keep as is)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showRecordingSheet = true },
                        onLongPress = { showRecordingSheet = true }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add_new_note_button),
                contentDescription = "Add New Note",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        }

        if (showRecordingSheet) {
            RecordingBottomSheet(
                viewModel = viewModel,
                onClose = { showRecordingSheet = false },
                onSave = { audioFilePath ->
                    showRecordingSheet = false
                    navController.navigate(Screen.CreateRecord.passPath(audioFilePath))
                }
            )
        }

        // --- FILTER SHEET DISPLAYS ---
        if (showMoodFilterSheet) {
            MoodFilterSheet(
                viewModel = viewModel,
                onClose = { showMoodFilterSheet = false }
            )
        }

        // NEW: Topic Filter Sheet Display
        if (showTopicFilterSheet) {
            TopicFilterSheet(
                viewModel = viewModel,
                onClose = { showTopicFilterSheet = false }
            )
        }
    }
}

// NEW: Filter Sheet Composable (Shows the list of moods for selection)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodFilterSheet(
    viewModel: HomeViewModel,
    onClose: () -> Unit
) {
    // Collect the current state of selected moods
    val selectedMoods by viewModel.selectedMoods.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        // Match the screenshot's aesthetic
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Mood",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            allAvailableMoods.forEach { mood ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleMoodFilter(mood) } // Toggles the mood filter
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mood Icon and Name
                    Text(text = mood, fontSize = 16.sp)

                    // Checkmark indicator (as seen in screenshot)
                    if (selectedMoods.contains(mood)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Selected",
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// NEW: Topic Filter Sheet Composable (Similar structure to MoodFilterSheet)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicFilterSheet(
    viewModel: HomeViewModel,
    onClose: () -> Unit
) {
    val selectedTopics by viewModel.selectedTopics.collectAsState() // RESOLVED: Used the correct ViewModel state

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Topics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            allAvailableTopics.forEach { topic ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleTopicFilter(topic) } // RESOLVED: Used the correct ViewModel method
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Topic Name (Display with hashtag for clarity)
                    Text(text = "#$topic", fontSize = 16.sp)

                    // Checkmark indicator
                    if (selectedTopics.contains(topic)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Selected",
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}


// NEW: Composable for the clickable filter button in the header (e.g., "Sad, Neutral")
@Composable
fun MoodFilterButton(
    currentMoods: Set<String>,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val buttonText = when {
        currentMoods.isEmpty() -> "All Moods"
        currentMoods.size == 1 -> currentMoods.first()
        else -> "${currentMoods.first()}, +${currentMoods.size - 1}" // Display first mood + count
    }

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                if (currentMoods.isNotEmpty()) Color(0xFFE6F0FF) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buttonText,
            fontSize = 12.sp,
            color = if (currentMoods.isNotEmpty()) Color(0xFF4A90E2) else Color.Gray,
            fontWeight = if (currentMoods.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
        )

        // Show clear button (X) if filters are active
        if (currentMoods.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Clear Filter",
                tint = Color(0xFF4A90E2),
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onClear)
            )
        }
    }
}

// NEW: Composable for the clickable Topic filter button in the header (e.g., "Work, +2")
@Composable
fun TopicFilterButton(
    currentTopics: Set<String>,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val buttonText = when {
        currentTopics.isEmpty() -> "All Topics"
        currentTopics.size == 1 -> currentTopics.first()
        else -> "${currentTopics.first()}, +${currentTopics.size - 1}" // Display first topic + count
    }

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                if (currentTopics.isNotEmpty()) Color(0xFFE6F0FF) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buttonText,
            fontSize = 12.sp,
            color = if (currentTopics.isNotEmpty()) Color(0xFF4A90E2) else Color.Gray,
            fontWeight = if (currentTopics.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
        )

        // Show clear button (X) if filters are active
        if (currentTopics.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Clear Filter",
                tint = Color(0xFF4A90E2),
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onClear)
            )
        }
    }
}


// NEW Composable to display a single journal entry card
@Composable
fun JournalEntryCard(entry: JournalEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // MOOD ICON
                Image(
                    painter = painterResource(
                        id = when (entry.mood) {
                            "stressed" -> R.drawable.ic_mood_stressed
                            "sad" -> R.drawable.ic_mood_sad
                            "neutral" -> R.drawable.ic_mood_neutral
                            "peaceful" -> R.drawable.ic_mood_peaceful
                            "excited" -> R.drawable.ic_mood_excited
                            else -> R.drawable.ic_mood_neutral
                        }
                    ),
                    contentDescription = "Mood: ${entry.mood}",
                    modifier = Modifier.size(24.dp)
                )

                // TITLE & TIME
                Row(
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = entry.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = entry.formattedTime,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // AUDIO PLAYBACK BAR (Reusing the player logic)
            PlaybackBarForEntry(entry.audioFilePath)

            Spacer(Modifier.height(8.dp))

            // DESCRIPTION SNIPPET
            if (entry.description.isNotBlank()) {
                Text(
                    text = entry.description.take(100) + if (entry.description.length > 100) "... Show more" else "",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
            Spacer(Modifier.height(8.dp))

            // TAGS
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE7E7E7), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("# $tag", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// REUSABLE PLAYER LOGIC (Centralized here)
// =======================================================

@Composable
fun PlaybackBarForEntry(audioFilePath: String) {
    // Each card manages its own playback state
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(0) }

    // MediaPlayer initialization is wrapped in remember(key) to ensure one player per audio file
    val mediaPlayer = remember(audioFilePath) {
        try {
            // Using the full package name for MediaPlayer since the import might not be explicit in the Composable
            android.media.MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                totalDuration = duration
                // Set listener to reset state when playback finishes
                setOnCompletionListener { isPlaying = false; currentTime = 0 }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }?.let { // Corrected syntax here
        // Dispose of the player when the composable leaves the screen (or recomposes with a new file)
        DisposableEffect(Unit) {
            onDispose {
                it.release()
            }
        }
        it // return the MediaPlayer instance
    }

    // Time tracking effect
    LaunchedEffect(isPlaying) {
        while (isPlaying && mediaPlayer != null) {
            currentTime = mediaPlayer.currentPosition
            delay(300)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Use the F5F5F5 background with slight rounding to match the CreateRecordScreen player bar look
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause Button
        Image(
            painter = painterResource(
                id = if (isPlaying)
                    R.drawable.ic_pause // Placeholder for pause button
                else
                    R.drawable.ic_play_button_create_rec // Assumed to be the green play icon
            ),
            contentDescription = "Play/Pause",
            modifier = Modifier
                .size(36.dp)
                // Only allow click if media player successfully initialized
                .clickable(enabled = mediaPlayer != null) {
                    if (mediaPlayer == null) return@clickable

                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        // Reset to start if playback finished
                        if (currentTime >= totalDuration) {
                            mediaPlayer.seekTo(0)
                        }
                        mediaPlayer.start()
                        isPlaying = true
                    }
                }
        )

        Spacer(Modifier.width(16.dp))

        // Linear Progress Bar (Mimics the waveform progress)
        LinearProgressIndicator(
            progress = if (totalDuration == 0 || mediaPlayer == null) 0f else currentTime / totalDuration.toFloat(),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF4A90E2)
        )

        Spacer(Modifier.width(16.dp))

        // Time Text
        Text(
            text = if (mediaPlayer == null) "Error" else "${formatTime(currentTime)} / ${formatTime(totalDuration)}",
            fontSize = 14.sp
        )
    }
}


// Utility function for time formatting (Centralized here for use across the package)
fun formatTime(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    // FIX: Mock necessary dependencies for the Preview
    // The mock object must implement the abstract function's full signature,
    // including optional parameters defined in the Dao interface.
    val mockDao = object : JournalEntryDao {
        override fun getAllEntries() = flowOf<List<JournalEntry>>(emptyList()) // FIX: Explicit type hint

        // FIX: Implement the full signature of getFilteredEntries
        override fun getFilteredEntries(
            moods: List<String>,
            tags: List<String>,
            moodsIsEmpty: Boolean,
            tagsIsEmpty: Boolean
        ): Flow<List<JournalEntry>> = flowOf(emptyList())

        override suspend fun insertEntry(entry: JournalEntry) {}
        override suspend fun deleteEntry(entry: JournalEntry) {}
    }
    val mockRepo = JournalRepository(mockDao)

    // FIX: Provide the required 'repository' argument to the HomeViewModel constructor
    val mockViewModel = HomeViewModel(mockRepo).apply {
        // Add a mock entry to the ViewModel using the mocked repository
        addEntry(JournalEntry("1", "My Entry", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "path", "peaceful", listOf("Work", "Conundrums")))
    }

    HomeScreen(
        navController = rememberNavController(),
        viewModel = mockViewModel
    )
}