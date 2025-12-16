package com.example.echojournal.presentation.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echojournal.R
import com.example.echojournal.data.local.dao.JournalEntryDao
import com.example.echojournal.data.local.entity.JournalEntry
import com.example.echojournal.data.repository.JournalRepository
import com.example.echojournal.domain.model.Mood
import com.example.echojournal.presentation.history.HistoryViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingBottomSheet(
    viewModel: HistoryViewModel,
    onClose: () -> Unit,
    onSave: (String) -> Unit,
    isPreview: Boolean = false
) {
    val context = LocalContext.current

    // --- COLLECT STATE FROM VIEWMODEL ---
    val duration by viewModel.recordDuration.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    // ------------------------------------

    // Format mm:ss
    val formattedTime = String.format(
        "%02d:%02d",
        TimeUnit.SECONDS.toMinutes(duration.toLong()),
        duration % 60
    )

    // Start recording automatically (if not in preview)
    LaunchedEffect(Unit) {
        if (!isPreview) {
            val file = File(context.filesDir, "echo_${System.currentTimeMillis()}.mp4")
            viewModel.startRecording(file)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.stopRecording()
            onClose()
        },
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close (X) top-right
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    viewModel.stopRecording()
                    onClose()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Cancel",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = when {
                    isPaused -> "Recording paused"
                    isRecording -> "Recording your memories..."
                    else -> "Preparing..."
                },
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Timer (live)
            Text(
                text = formattedTime,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left button (close/cancel)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEAEA)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        viewModel.stopRecording()
                        onClose()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Center button (Mic / Resume / Check logic can vary)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF007BFF)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (!isPaused) viewModel.pauseRecording()
                            else viewModel.resumeRecording()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // While recording, show Check (to save) or Mic (visual)
                        // This logic depends on UX preferences. Usually Center = Action.
                        // Let's use Mic while recording, Check when Paused to complete?
                        // Based on typical assignment: Center button often toggles recording.
                        Icon(
                            painter = painterResource(
                                // If running: Mic icon (or Pause icon). If Paused: Mic to resume.
                                id = if (isPaused) R.drawable.ic_mic else R.drawable.ic_mic
                            ),
                            contentDescription = "Toggle Recording",
                            tint = Color.White, // Contrast on Blue
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                        )
                    }
                }

                // Right button (Pause / Save check)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6F0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        if (isPaused) {
                            // If paused -> Save and finish
                            viewModel.stopRecording()
                            viewModel.recordedFilePath?.let { path ->
                                onSave(path)
                            }
                        } else {
                            // If recording -> Pause
                            viewModel.pauseRecording()
                        }
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPaused) R.drawable.ic_check else R.drawable.ic_pause
                            ),
                            contentDescription = "Pause/Save",
                            tint = Color(0xFF007BFF), // Blue tint on light background
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ---------------------------------------------------------
// PREVIEW HELPERS
// ---------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFEFEFEF)
@Composable
fun RecordingBottomSheetPreview() {

    // 1. Define a Mock DAO with CORRECT signature
    val mockDao = object : JournalEntryDao {
        override fun getAllEntries() = flowOf<List<JournalEntry>>(emptyList())

        // FIX: Signature matches the updated DAO (List<Mood>, String?, Boolean)
        override fun getFilteredEntries(
            moods: List<Mood>,
            topicsQuery: String?,
            moodsIsEmpty: Boolean
        ): Flow<List<JournalEntry>> = flowOf(emptyList())

        override suspend fun insertEntry(entry: JournalEntry) {}

        override suspend fun deleteEntryById(entryId: String) {}
    }

    val mockRepo = JournalRepository(mockDao)
    val fakeViewModel = HistoryViewModel(mockRepo)

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            RecordingBottomSheet(
                viewModel = fakeViewModel,
                onClose = {},
                onSave = {},
                isPreview = true // Prevents actual recording logic in Preview
            )
        }
    }
}