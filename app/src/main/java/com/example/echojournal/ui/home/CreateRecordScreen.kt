package com.example.echojournal.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.example.echojournal.data.model.JournalEntry
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecordScreen(
    navController: NavHostController? = null,
    audioFilePath: String,
    viewModel: HomeViewModel = viewModel()
) {
    var titleText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var currentTag by remember { mutableStateOf("") }
    var tagList by remember { mutableStateOf(listOf<String>()) }

    var selectedMood by remember { mutableStateOf<String?>(null) }
    var showMoodSheet by remember { mutableStateOf(false) }

    // NEW: Logic to determine if the form is valid (required fields are filled)
    val isFormValid = titleText.isNotBlank() && description.isNotBlank() && selectedMood != null

    val scrollState = rememberScrollState()

    if (showMoodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoodSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
        ) {
            MoodSelectionSheet(
                onMoodSelected = {
                    selectedMood = it
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
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {

            // TOP BAR
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
                        .clickable { navController?.popBackStack() }
                )

                Text(
                    text = "New Entry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Divider(color = Color.LightGray, thickness = 1.dp)

            // TITLE + EMOJI SLOT + PLUS BUTTON
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                AnimatedVisibility(visible = selectedMood != null) {
                    Image(
                        painter = painterResource(
                            id = when (selectedMood) {
                                "stressed" -> R.drawable.ic_mood_stressed
                                "sad" -> R.drawable.ic_mood_sad
                                "neutral" -> R.drawable.ic_mood_neutral
                                "peaceful" -> R.drawable.ic_mood_peaceful
                                "excited" -> R.drawable.ic_mood_excited
                                else -> R.drawable.ic_mood_neutral
                            }
                        ),
                        contentDescription = "Mood",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 10.dp)
                    )
                }

                Column(Modifier.weight(1f)) {
                    TextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        placeholder = { Text("Add title…", color = Color.Gray) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F8F8), RoundedCornerShape(10.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF8F8F8),
                            unfocusedContainerColor = Color(0xFFF8F8F8)
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_add_mood),
                    contentDescription = "Select Mood",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { showMoodSheet = true }
                )
            }

            Divider(color = Color.LightGray, thickness = 1.dp)

            // FIXED AUDIO PLAYER CALL
            // NOTE: RecordingPlaybackBar relies on formatTime, which is now accessed from HomeScreen.kt
            RecordingPlaybackBar(audioFilePath)

            Divider(color = Color.LightGray, thickness = 1.dp)

            // TAGS
            Column(modifier = Modifier.padding(16.dp)) {
                Text("# Topic", fontSize = 14.sp, color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))

                TextField(
                    value = currentTag,
                    onValueChange = { currentTag = it },
                    placeholder = { Text("Add tag…", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp)),
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
                                color = Color(0xFF4A90E2),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(12.dp))

                // FlowRow is correctly defined here
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tagList.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE7E7E7), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("# $tag", fontSize = 14.sp)
                        }
                    }
                }
            }

            // DESCRIPTION
            Column(modifier = Modifier.padding(16.dp)) {
                Text("# Description", fontSize = 14.sp, color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Add description…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // SAVE / CANCEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController?.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = {
                        // The JournalEntry class is now imported from the new package
                        val newEntry = JournalEntry(
                            title = titleText.ifBlank { "My Entry" },
                            description = description,
                            audioFilePath = audioFilePath,
                            mood = selectedMood,
                            tags = tagList
                        )
                        viewModel.addEntry(newEntry)
                        navController?.popBackStack()
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        disabledContainerColor = Color(0xFFC0DFFF)
                    )
                ) {
                    Text("Save", color = Color.White)
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun MoodSelectionSheet(onMoodSelected: (String) -> Unit) {
// ... (content remains) ...
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("How are you feeling?", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoodIcon("stressed", R.drawable.ic_mood_stressed, onMoodSelected)
            MoodIcon("sad", R.drawable.ic_mood_sad, onMoodSelected)
            MoodIcon("neutral", R.drawable.ic_mood_neutral, onMoodSelected)
            MoodIcon("peaceful", R.drawable.ic_mood_peaceful, onMoodSelected)
            MoodIcon("excited", R.drawable.ic_mood_excited, onMoodSelected)
        }
    }
}

@Composable
fun MoodIcon(mood: String, iconRes: Int, onClick: (String) -> Unit) {
// ... (content remains) ...
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(mood) }
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = mood,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
fun RecordingPlaybackBar(audioFilePath: String) {
// ... (content remains) ...
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(0) }

    val mediaPlayer = remember {
        android.media.MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepare()
            totalDuration = duration
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentTime = mediaPlayer.currentPosition
            delay(300)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(
                id = if (isPlaying)
                    R.drawable.ic_pause
                else
                    R.drawable.ic_play_button_create_rec
            ),
            contentDescription = "Play/Pause",
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        mediaPlayer.start()
                        isPlaying = true
                    }
                }
        )

        Spacer(Modifier.width(16.dp))

        LinearProgressIndicator(
            progress = if (totalDuration == 0) 0f else currentTime / totalDuration.toFloat(),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF4A90E2)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            // This relies on the formatTime function defined in HomeScreen.kt
            text = "${formatTime(currentTime)} / ${formatTime(totalDuration)}",
            fontSize = 14.sp
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
}