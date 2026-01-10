package com.andreasgks.echojournal.presentation.history

import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.andreasgks.echojournal.R
import com.andreasgks.echojournal.data.local.entity.JournalEntry
import com.andreasgks.echojournal.domain.model.Mood
import com.andreasgks.echojournal.presentation.navigation.Screen
import com.andreasgks.echojournal.presentation.record.RecordingBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O) // Required for LocalDate
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel = viewModel(),
    fromWidget: Boolean = false
) {
    // --- STATE MANAGEMENT ---
    var showRecordingSheet by remember { mutableStateOf(false) }
    var showMoodFilterSheet by remember { mutableStateOf(false) }
    var showTopicFilterSheet by remember { mutableStateOf(false) }

    // Auto-open recording if from widget
    LaunchedEffect(fromWidget) {
        if (fromWidget) {
            showRecordingSheet = true
        }
    }

    // Collect Data from ViewModel
    val currentMoods by viewModel.selectedMoods.collectAsState()
    val currentTopics by viewModel.selectedTopics.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val entries: List<JournalEntry> by viewModel.journalEntries.collectAsState()

    val showEmptyState = entries.isEmpty()

    // Scaffold State for Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
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

                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                EchoJournalSearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: Filter Buttons
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
                    // Empty State
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

                        val emptyText = if (searchQuery.isNotBlank()) "No matches found" else "No Entries"

                        Text(
                            text = emptyText,
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
                    // List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Removed the hardcoded "TODAY" header to avoid confusion.
                        // You can add logic here to group items by date headers if you wish in V2.

                        items(entries, key = { entry -> entry.id }) { entry ->
                            SwipeableJournalEntryItem(
                                entry = entry,
                                onDismiss = { deletedEntry ->
                                    scope.launch {
                                        viewModel.deleteJournalEntry(deletedEntry.id)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Entry deleted",
                                            actionLabel = "Undo",
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.addEntry(deletedEntry)
                                        }
                                    }
                                },
                                onClick = { clickedEntry ->
                                    navController.navigate(Screen.JournalEntry.edit(clickedEntry.id))
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // FAB
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
                    navController.navigate(Screen.JournalEntry.create(audioFilePath))
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

// --- NEW COMPONENT: SEARCH BAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EchoJournalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search your entries...", color = Color.Gray) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Clear",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onQueryChange("") }
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFFF2F2F7), RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFF007BFF)
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

// --- SWIPEABLE ITEM ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableJournalEntryItem(
    entry: JournalEntry,
    onDismiss: (JournalEntry) -> Unit,
    onClick: (JournalEntry) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart ||
                dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDismiss(entry)
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = { DismissBackground(dismissState) }
    ) {
        TimelineJournalEntryItem(entry = entry, onClick = onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val deleteColor = Color(0xFFFF3B30)
    val color = when (dismissState.targetValue) {
        SwipeToDismissBoxValue.Settled -> Color.Transparent
        else -> deleteColor
    }

    val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
        Alignment.CenterStart
    } else {
        Alignment.CenterEnd
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
        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(28.dp).scale(iconScale)
            )
        }
    }
}

// --- TIMELINE ITEM ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimelineJournalEntryItem(
    entry: JournalEntry,
    onClick: (JournalEntry) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F2F7))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
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
        JournalEntryCard(entry = entry, onClick = { onClick(entry) })
    }
}

// --- CARD COMPONENT ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

                // UPDATED: Using the real date function
                Text(
                    text = getRelativeDate(entry.timestamp),
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

// ----------------------------------------------------
// HELPER FUNCTIONS
// ----------------------------------------------------
fun formatTime(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}

// NEW: Date Formatter Logic
@RequiresApi(Build.VERSION_CODES.O)
fun getRelativeDate(date: Date): String {
    val now = LocalDate.now()
    val entryDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    return when {
        entryDate.isEqual(now) -> "Today"
        entryDate.isEqual(now.minusDays(1)) -> "Yesterday"
        else -> entryDate.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
    }
}

// --- FILTER BUTTONS & SHEETS (Keep unchanged) ---

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
    val allMoods = Mood.entries

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
    val allAvailableTopics by viewModel.availableTopics.collectAsState()

    ModalBottomSheet(onDismissRequest = onClose, containerColor = Color.White) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Filter by Topics", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(16.dp))

            if (allAvailableTopics.isEmpty()) {
                Text(
                    "No topics created yet.",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

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