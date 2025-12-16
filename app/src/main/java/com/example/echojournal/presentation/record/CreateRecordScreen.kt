package com.example.echojournal.presentation.record

import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echojournal.R
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.domain.model.Mood
import com.example.echojournal.presentation.history.HistoryViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateRecordScreen(
    navController: NavHostController? = null,
    audioFilePath: String,
    viewModel: HistoryViewModel = viewModel()
) {
    var titleText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var currentTag by remember { mutableStateOf("") }
    var tagList by remember { mutableStateOf(listOf<String>()) }

    // Use Mood Enum here instead of String?
    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var showMoodSheet by remember { mutableStateOf(false) }

    // Valid if Title + Description + Mood are present
    val isFormValid = titleText.isNotBlank() && description.isNotBlank() && selectedMood != null

    val scrollState = rememberScrollState()

    if (showMoodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoodSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            dragHandle = null
        ) {
            MoodSelectionSheet(
                onMoodSelected = { mood ->
                    selectedMood = mood
                    showMoodSheet = false
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color.White)
                .statusBarsPadding()
        ) {

            // --- TOP BAR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back_button_create_rec),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { navController?.popBackStack() }
                )

                Text(
                    text = "New Entry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            // --- TITLE + MOOD TOGGLE BUTTON ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    TextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        placeholder = {
                            Text("Add Title...", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Mood Toggle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2F2F7))
                        .clickable {
                            if (selectedMood == null) {
                                showMoodSheet = true
                            } else {
                                selectedMood = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedMood != null) {
                        Image(
                            painter = painterResource(id = selectedMood!!.iconRes),
                            contentDescription = selectedMood!!.name,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_mood),
                            contentDescription = "Select Mood",
                            tint = Color(0xFF007BFF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Audio Player
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                CreateRecordPlaybackBar(audioFilePath)
            }

            Spacer(Modifier.height(24.dp))

            // --- TAGS / TOPICS ---
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "TOPIC",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = currentTag,
                        onValueChange = { currentTag = it },
                        placeholder = { Text("Add topic...", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFF8F8F8), RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            if (currentTag.isNotBlank()) {
                                Text(
                                    "Add",
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            tagList = tagList + currentTag.trim()
                                            currentTag = ""
                                        },
                                    color = Color(0xFF007BFF),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tagList.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF2F2F7), RoundedCornerShape(50.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("# $tag", fontSize = 14.sp, color = Color.Black)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- DESCRIPTION ---
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "DESCRIPTION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Add description...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFFF8F8F8), RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(Modifier.height(32.dp))

            // --- BUTTONS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController?.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.width(16.dp))

                Button(
                    onClick = {
                        val newEntry = JournalEntry(
                            title = titleText.ifBlank { "My Entry" },
                            description = description,
                            audioFilePath = audioFilePath,
                            mood = selectedMood ?: Mood.Neutral, // Default safe-guard
                            topics = tagList
                        )
                        viewModel.addEntry(newEntry)
                        navController?.popBackStack()
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007BFF),
                        disabledContainerColor = Color(0xFFB3D7FF)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Save", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun MoodSelectionSheet(onMoodSelected: (Mood) -> Unit) {
    val moods = Mood.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("How are you feeling?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            moods.forEach { mood ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onMoodSelected(mood) }
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = mood.iconRes),
                        contentDescription = mood.name,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mood.name,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CreateRecordPlaybackBar(audioFilePath: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(0) }

    val mediaPlayer = remember {
        try {
            MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                totalDuration = duration
                setOnCompletionListener {
                    isPlaying = false
                    currentTime = 0
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
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
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape)
                .clickable(enabled = mediaPlayer != null) {
                    mediaPlayer?.let { player ->
                        if (isPlaying) {
                            player.pause()
                            isPlaying = false
                        } else {
                            if (currentTime >= totalDuration) player.seekTo(0)
                            player.start()
                            isPlaying = true
                        }
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
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        LinearProgressIndicator(
            progress = if (totalDuration == 0) 0f else currentTime / totalDuration.toFloat(),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF007BFF),
            trackColor = Color(0xFFCDE0FF)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = "${formatTimeInternal(currentTime)}/${formatTimeInternal(totalDuration)}",
            fontSize = 14.sp,
            color = Color(0xFF007BFF),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(4.dp))
    }
}

// Internal helper logic specifically for this screen
private fun formatTimeInternal(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}