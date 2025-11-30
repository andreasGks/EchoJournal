package com.example.echojournal.ui.home

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.echojournal.data.model.JournalEntryDao
import com.example.echojournal.data.repository.JournalRepository
import com.example.echojournal.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// --- DATA CONSTANTS ---
val allAvailableMoods = listOf("Excited", "Peaceful", "Neutral", "Sad", "Stressed")
val allAvailableTopics = listOf("Work", "Family", "Health", "Study", "Travel", "Random")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    // --- STATE MANAGEMENT ---
    var showRecordingSheet by remember { mutableStateOf(false) }
    var showMoodFilterSheet by remember { mutableStateOf(false) }
    var showTopicFilterSheet by remember { mutableStateOf(false) }

    // Collect Data from ViewModel
    val currentMoods by viewModel.selectedMoods.collectAsState()
    val currentTopics by viewModel.selectedTopics.collectAsState()
    val entries: List<JournalEntry> by viewModel.journalEntries.collectAsState()
    val showEmptyState = entries.isEmpty()

    // Main Scaffold-like structure using Box to handle the Floating Action Button
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Main background
    ) {

        // Content Column: Splits screen into [Header] and [List Area]
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==========================================
            // NEW HEADER DESIGN
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
            ) {
                // Row 1: Title and Settings Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your EchoJournal",
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { /* Handle Settings */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: Filter Buttons (Chips)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodFilterButton(
                        currentMoods = currentMoods,
                        onClick = { showMoodFilterSheet = true },
                        onClear = { viewModel.clearMoodFilters() }
                    )

                    TopicFilterButton(
                        currentTopics = currentTopics,
                        onClick = { showTopicFilterSheet = true },
                        onClear = { viewModel.clearTopicFilters() }
                    )
                }
            }

            // ==========================================
            // DYNAMIC CONTENT (List or Empty State)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (showEmptyState) {
                    // Empty State Centered in the remaining space
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
                    // Scrollable List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp) // Space for FAB
                    ) {
                        item {
                            Text(
                                text = "TODAY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                            )
                        }
                        items(entries, key = { entry -> entry.id }) { entry ->
                            JournalEntryCard(entry = entry)
                        }
                    }
                }
            }
        }

        // ==========================================
        // FLOATING ACTION BUTTON
        // ==========================================
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

        // --- SHEETS ---
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

        if (showMoodFilterSheet) {
            MoodFilterSheet(
                viewModel = viewModel,
                onClose = { showMoodFilterSheet = false }
            )
        }

        if (showTopicFilterSheet) {
            TopicFilterSheet(
                viewModel = viewModel,
                onClose = { showTopicFilterSheet = false }
            )
        }
    }
}

// --- BOTTOM SHEETS UI ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodFilterSheet(
    viewModel: HomeViewModel,
    onClose: () -> Unit
) {
    val selectedMoods by viewModel.selectedMoods.collectAsState()

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
                        .clickable { viewModel.toggleMoodFilter(mood) }
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = mood, fontSize = 16.sp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicFilterSheet(
    viewModel: HomeViewModel,
    onClose: () -> Unit
) {
    val selectedTopics by viewModel.selectedTopics.collectAsState()

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
                        .clickable { viewModel.toggleTopicFilter(topic) }
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "#$topic", fontSize = 16.sp)
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

// --- FILTER BUTTONS ---

@Composable
fun MoodFilterButton(
    currentMoods: Set<String>,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val isActive = currentMoods.isNotEmpty()

    val buttonText = when {
        currentMoods.isEmpty() -> "All Moods"
        currentMoods.size == 1 -> currentMoods.first()
        else -> "${currentMoods.first()} +${currentMoods.size - 1}"
    }

    val backgroundColor = if (isActive) Color(0xFFE6F0FF) else Color.White
    val borderColor = if (isActive) Color.Transparent else Color(0xFFE0E0E0)
    val textColor = if (isActive) Color(0xFF4A90E2) else Color(0xFF333333)
    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buttonText,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = fontWeight
        )

        if (isActive) {
            Spacer(Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Clear Filter",
                tint = textColor,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onClear)
            )
        }
    }
}

@Composable
fun TopicFilterButton(
    currentTopics: Set<String>,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val isActive = currentTopics.isNotEmpty()

    val buttonText = when {
        currentTopics.isEmpty() -> "All Topics"
        currentTopics.size == 1 -> currentTopics.first()
        else -> "${currentTopics.first()} +${currentTopics.size - 1}"
    }

    val backgroundColor = if (isActive) Color(0xFFE6F0FF) else Color.White
    val borderColor = if (isActive) Color.Transparent else Color(0xFFE0E0E0)
    val textColor = if (isActive) Color(0xFF4A90E2) else Color(0xFF333333)
    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buttonText,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = fontWeight
        )

        if (isActive) {
            Spacer(Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Clear Filter",
                tint = textColor,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onClear)
            )
        }
    }
}

// --- JOURNAL ENTRY CARD & AUDIO PLAYER ---

@OptIn(ExperimentalLayoutApi::class)
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
                // Mood Icon
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

                // Title & Time
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

            PlaybackBarForEntry(entry.audioFilePath)

            Spacer(Modifier.height(8.dp))

            if (entry.description.isNotBlank()) {
                Text(
                    text = entry.description.take(100) + if (entry.description.length > 100) "... Show more" else "",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
            Spacer(Modifier.height(8.dp))

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

@Composable
fun PlaybackBarForEntry(audioFilePath: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(0) }

    val mediaPlayer = remember(audioFilePath) {
        try {
            MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                totalDuration = duration
                setOnCompletionListener { isPlaying = false; currentTime = 0 }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }?.let {
        DisposableEffect(Unit) {
            onDispose { it.release() }
        }
        it
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying && mediaPlayer != null) {
            currentTime = mediaPlayer.currentPosition
            delay(300)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_button_create_rec
            ),
            contentDescription = "Play/Pause",
            modifier = Modifier
                .size(36.dp)
                .clickable(enabled = mediaPlayer != null) {
                    if (mediaPlayer == null) return@clickable
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        if (currentTime >= totalDuration) mediaPlayer.seekTo(0)
                        mediaPlayer.start()
                        isPlaying = true
                    }
                }
        )

        Spacer(Modifier.width(16.dp))

        LinearProgressIndicator(
            progress = if (totalDuration == 0 || mediaPlayer == null) 0f else currentTime / totalDuration.toFloat(),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF4A90E2)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = if (mediaPlayer == null) "Error" else "${formatTime(currentTime)} / ${formatTime(totalDuration)}",
            fontSize = 14.sp
        )
    }
}

fun formatTime(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}

// --- PREVIEW ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val mockDao = object : JournalEntryDao {
        override fun getAllEntries() = flowOf<List<JournalEntry>>(emptyList())
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
    val mockViewModel = HomeViewModel(mockRepo).apply {
        addEntry(JournalEntry("1", "My Entry", "Lorem ipsum dolor sit amet.", "path", "peaceful", listOf("Work")))
    }

    HomeScreen(
        navController = rememberNavController(),
        viewModel = mockViewModel
    )
}