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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echojournal.R
import com.example.echojournal.presentation.history.HistoryViewModel
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

                // Center button (Mic / Resume)
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
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_mic // Kept simple as per your request
                            ),
                            contentDescription = "Toggle Recording",
                            tint = Color.White,
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
                            tint = Color(0xFF007BFF),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}