package com.andreasgks.echojournal.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.andreasgks.echojournal.R
import com.andreasgks.echojournal.domain.model.Mood

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val defaultMood by viewModel.defaultMood.collectAsState()
    val defaultTopics by viewModel.defaultTopics.collectAsState()
    val availableTopics by viewModel.availableTopics.collectAsState()

    var showTopicInput by remember { mutableStateOf(false) }
    var newTopicText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SettingsTopBar(onBackClick = { navController.popBackStack() })
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // --- SECTION 1: MY MOOD ---
            Text(
                text = "My Mood",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Select default mood to apply to all new entries",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Mood.entries.forEach { mood ->
                    MoodItem(
                        mood = mood,
                        isSelected = mood == defaultMood,
                        onClick = { viewModel.setDefaultMood(mood) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 2: MY TOPICS ---
            Text(
                text = "My Topics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Select default topics to apply to all new entries. Long press to delete.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. List existing topics
                availableTopics.forEach { topic ->
                    TopicChip(
                        text = topic,
                        isSelected = defaultTopics.contains(topic),
                        onClick = { viewModel.toggleDefaultTopic(topic) },
                        onLongClick = { viewModel.deleteTopic(topic) } // NEW: Delete on Long Press
                    )
                }

                // 2. Add New Topic Button / Input
                if (showTopicInput) {
                    OutlinedTextField(
                        value = newTopicText,
                        onValueChange = { newTopicText = it },
                        placeholder = { Text("New Topic", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier
                            .width(120.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedBorderColor = Color(0xFF007BFF),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newTopicText.isNotBlank()) {
                                viewModel.createNewTopic(newTopicText)
                                newTopicText = ""
                            }
                            showTopicInput = false
                        })
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .background(Color(0xFFF2F2F7), CircleShape)
                            .clickable { showTopicInput = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_mood),
                            contentDescription = "Add Topic",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SUB COMPONENTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_button_create_rec),
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun MoodItem(mood: Mood, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        val backgroundColor = if (isSelected) Color(0xFFE6F0FF) else Color.Transparent
        val borderColor = if (isSelected) Color(0xFF007BFF) else Color.Transparent

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(backgroundColor, CircleShape)
                .border(2.dp, borderColor, CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = mood.iconRes),
                contentDescription = mood.name,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mood.name,
            fontSize = 12.sp,
            color = if (isSelected) Color.Black else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopicChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFE6F0FF) else Color(0xFFF2F2F7)
    val textColor = if (isSelected) Color(0xFF007BFF) else Color.Black
    val borderStroke = if (isSelected) BorderStroke(1.dp, Color(0xFF007BFF)) else null

    // We use Surface purely for styling, but apply clicks to the Modifier
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50.dp),
        border = borderStroke,
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(50.dp)) // Needed so ripple effect respects the shape
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "# $text",
                    fontSize = 14.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}