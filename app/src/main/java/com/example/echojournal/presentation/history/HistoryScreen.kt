package com.example.echojournal.presentation.history

import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.echojournal.R
import com.example.echojournal.data.local.dao.JournalEntryDao
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.data.repository.JournalRepository
import com.example.echojournal.domain.model.Mood
import com.example.echojournal.presentation.navigation.Screen
import com.example.echojournal.presentation.record.RecordingBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// --- DATA CONSTANTS ---
val allAvailableTopics = listOf("Work", "Family", "Health", "Study", "Travel", "Random")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel = viewModel()
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

    // Scaffold State for Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Main Scaffold-like structure using Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)) // Light Gray Background
    ) {

        // Content Column: Splits screen into [Header] and [List Area]
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==========================================
            // HEADER
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
                    // Empty State Centered
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
                    // Scrollable List with Timeline Look
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "TODAY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            )
                        }
                        // Use SwipeableJournalEntryItem for delete functionality
                        // KEY is important here to prevent state bugs when items move/add
                        items(entries, key = { entry -> entry.id }) { entry ->
                            SwipeableJournalEntryItem(
                                entry = entry,
                                onDismiss = { deletedEntry ->
                                    // Show Snackbar with Undo option
                                    scope.launch {
                                        // 1. Delete temporarily
                                        viewModel.deleteJournalEntry(deletedEntry.id)

                                        val result = snackbarHostState.showSnackbar(
                                            message = "Entry deleted",
                                            actionLabel = "Undo",
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            // Restore if UNDO is pressed
                                            viewModel.addEntry(deletedEntry)
                                        }
                                    }
                                }
                            )
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

        // ==========================================
        // SNACKBAR HOST (For Undo)
        // ==========================================
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // --- SHEETS ---
        if (showRecordingSheet) {
            RecordingBottomSheet(
                viewModel = viewModel,
                onClose = { showRecordingSheet = false },
                onSave = { audioFilePath ->
                    showRecordingSheet = false
                    navController.navigate(Screen.CreateRecord.passPath(audioFilePath))
                },
                isPreview = false
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

// --- NEW COMPONENT: SWIPEABLE ITEM WRAPPER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableJournalEntryItem(
    entry: JournalEntry,
    onDismiss: (JournalEntry) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            // FIX: Allow both directions (Left to Right OR Right to Left)
            if (dismissValue == SwipeToDismissBoxValue.EndToStart ||
                dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDismiss(entry)
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.35f } // 35% swipe required
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true, // Allow Left Swipe
        enableDismissFromEndToStart = true, // Allow Right Swipe
        backgroundContent = {
            DismissBackground(dismissState)
        }
    ) {
        TimelineJournalEntryItem(entry = entry)
    }
}

// --- NEW COMPONENT: SWIPE BACKGROUND ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    // Standard "Destructive" Red (Used in modern iOS/Android apps)
    val deleteColor = Color(0xFFFF3B30)

    // FIX: Only show color when NOT Settled (Idle).
    // This prevents the red background from showing up on new items.
    val color = when (dismissState.targetValue) {
        SwipeToDismissBoxValue.Settled -> Color.Transparent
        else -> deleteColor
    }

    // Logic to switch icon side based on swipe direction
    val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
        Alignment.CenterStart // Swiping Right -> Icon on Left
    } else {
        Alignment.CenterEnd   // Swiping Left -> Icon on Right
    }

    val iconScale by animateFloatAsState(
        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 0.8f,
        label = "iconScale"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 30.dp),
        contentAlignment = alignment
    ) {
        // Only show the trash icon if the user is actively swiping
        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close), // Ideally replace with ic_delete if you have it
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .scale(iconScale)
            )
        }
    }
}


// --- EXISTING COMPONENT: TIMELINE ITEM ---
@Composable
fun TimelineJournalEntryItem(entry: JournalEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F2F7)) // IMPORTANT: Matches screen bg so swipe reveals color underneath
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 1. Mood Icon Column (Outside Card)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            val moodResId = entry.mood.iconRes

            Image(
                painter = painterResource(id = moodResId),
                contentDescription = "Mood: ${entry.mood.name}",
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. The Content Card
        JournalEntryCard(entry = entry)
    }
}

// --- CARD COMPONENT ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalEntryCard(entry: JournalEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Today", // Placeholder
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(12.dp))

            PlaybackBarForEntry(entry.audioFilePath)

            Spacer(Modifier.height(12.dp))

            if (entry.description.isNotBlank()) {
                Text(
                    text = entry.description.take(100) + if (entry.description.length > 100) "... Show more" else "",
                    fontSize = 15.sp,
                    color = Color(0xFF444444),
                    lineHeight = 22.sp
                )
            }

            if (entry.topics.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.topics.forEach { topic ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "#$topic",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
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
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFFE6F0FF))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.White, CircleShape)
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
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_button_create_rec
                ),
                contentDescription = "Play/Pause",
                tint = Color(0xFF007BFF),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        LinearProgressIndicator(
            progress = if (totalDuration == 0 || mediaPlayer == null) 0f else currentTime / totalDuration.toFloat(),
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF007BFF),
            trackColor = Color(0xFFCDE0FF)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = if (mediaPlayer == null) "Error" else "${formatTime(currentTime)}/${formatTime(totalDuration)}",
            fontSize = 12.sp,
            color = Color(0xFF007BFF),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(8.dp))
    }
}

fun formatTime(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}

// --- FILTER BUTTONS & SHEETS ---

@Composable
fun MoodFilterButton(
    currentMoods: Set<String>,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val isActive = currentMoods.isNotEmpty()
    val selectedMoods = currentMoods.map { Mood.fromName(it) }

    val backgroundColor = Color.White
    val borderColor = if (isActive) Color(0xFF007BFF) else Color(0xFFE0E0E0)
    val shadowElevation = if (isActive) 4.dp else 0.dp

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = shadowElevation,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isActive) {
                Text("All Moods", fontSize = 14.sp, color = Color.Black)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    selectedMoods.take(3).forEach { mood ->
                        Image(
                            painter = painterResource(id = mood.iconRes),
                            contentDescription = mood.name,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (selectedMoods.size > 3) {
                        Text(
                            text = "+${selectedMoods.size - 3}",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Clear",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp).clickable(onClick = onClear)
                )
            }
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

    val backgroundColor = Color.White
    val borderColor = if (isActive) Color(0xFF007BFF) else Color(0xFFE0E0E0)
    val shadowElevation = if (isActive) 4.dp else 0.dp

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = shadowElevation,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(buttonText, fontSize = 14.sp, color = Color.Black)
            if (isActive) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Clear",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp).clickable(onClick = onClear)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodFilterSheet(viewModel: HistoryViewModel, onClose: () -> Unit) {
    val selectedMoods by viewModel.selectedMoods.collectAsState()
    val allMoods = Mood.entries // Use Enum entries

    ModalBottomSheet(onDismissRequest = onClose, containerColor = Color.White) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Filter by Mood", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            allMoods.forEach { mood ->
                val isSelected = selectedMoods.contains(mood.name)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleMoodFilter(mood.name) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = mood.iconRes),
                            contentDescription = mood.name,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(mood.name, fontSize = 16.sp, color = Color.Black)
                    }
                    if (isSelected) {
                        Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = Color(0xFF007BFF))
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicFilterSheet(viewModel: HistoryViewModel, onClose: () -> Unit) {
    val selectedTopics by viewModel.selectedTopics.collectAsState()

    ModalBottomSheet(onDismissRequest = onClose, containerColor = Color.White) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Filter by Topics", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            allAvailableTopics.forEach { topic ->
                val isSelected = selectedTopics.contains(topic)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleTopicFilter(topic) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("#$topic", fontSize = 16.sp, color = Color.Black)
                    if (isSelected) {
                        Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null, tint = Color(0xFF007BFF))
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val mockDao = object : JournalEntryDao {
        override fun getAllEntries() = flowOf(emptyList<JournalEntry>())
        override fun getFilteredEntries(moods: List<Mood>, topicsQuery: String?, moodsIsEmpty: Boolean) = flowOf(
            listOf(
                JournalEntry(
                    title = "Preview Entry",
                    description = "Description",
                    audioFilePath = "",
                    mood = Mood.Peaceful,
                    topics = listOf("Work")
                )
            )
        )
        override suspend fun insertEntry(entry: JournalEntry) {}
        override suspend fun deleteEntryById(entryId: String) {}
    }
    val mockRepo = JournalRepository(mockDao)
    val mockViewModel = HistoryViewModel(mockRepo)
    HistoryScreen(rememberNavController(), mockViewModel)
}