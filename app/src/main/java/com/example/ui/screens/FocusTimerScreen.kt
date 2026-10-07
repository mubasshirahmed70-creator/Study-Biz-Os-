package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BusinessAccent
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyBizViewModel
import java.util.Locale

@Composable
fun FocusTimerScreen(
    viewModel: StudyBizViewModel,
    modifier: Modifier = Modifier
) {
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val totalSeconds by viewModel.totalDurationSeconds.collectAsStateWithLifecycle()
    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val topic by viewModel.timerTopic.collectAsStateWithLifecycle()
    val completedMessage by viewModel.timerCompletedMessage.collectAsStateWithLifecycle()
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()

    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var showChangeTopicDialog by remember { mutableStateOf(false) }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val progress = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds.toFloat()) else 0f

    val accentColor = when (timerMode) {
        "BUSINESS" -> BusinessAccent
        "BREAK" -> Color(0xFF10B981)
        else -> StudyPrimary
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ডিপ ফোকাস ইঞ্জিন",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "পরিকল্পিত ফোকাস সেশন",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "সেশন শেষে স্বয়ংক্রিয়ভাবে প্রগ্রেস ও ডেটাবেসে সময় যুক্ত হয়",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Completion banner
        if (completedMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("timer_completed_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = completedMessage ?: "",
                            color = Color(0xFFA7F3D0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.dismissTimerMessage() }) {
                            Text("ঠিক আছে", color = Color(0xFFA7F3D0))
                        }
                    }
                }
            }
        }

        // Mode Presets Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = timerMode == "STUDY",
                    onClick = { viewModel.selectTimerMode("STUDY", settings.studyFocusMinutes) },
                    label = { Text("পড়াশোনা (${settings.studyFocusMinutes} মি.)") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.weight(1f).testTag("timer_mode_study")
                )
                FilterChip(
                    selected = timerMode == "BREAK",
                    onClick = { viewModel.selectTimerMode("BREAK", settings.studyBreakMinutes) },
                    label = { Text("বিরতি (${settings.studyBreakMinutes} মি.)") },
                    leadingIcon = { Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.weight(1f).testTag("timer_mode_break")
                )
                FilterChip(
                    selected = timerMode == "BUSINESS",
                    onClick = { viewModel.selectTimerMode("BUSINESS", settings.businessFocusMinutes) },
                    label = { Text("ব্যবসা (${settings.businessFocusMinutes} মি.)") },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.weight(1f).testTag("timer_mode_biz")
                )
            }
        }

        // Active Topic Tag
        item {
            Surface(
                onClick = { showChangeTopicDialog = true },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("timer_topic_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Focusing on: $topic",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("✎ Edit", fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Circular Timer Display
        item {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .testTag("circular_timer_box"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    // Background track
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.15f),
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress arc
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRunning) "SESSION IN PROGRESS" else "PAUSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Timer Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetTimer() },
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp).testTag("timer_reset_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }

                Spacer(modifier = Modifier.width(20.dp))

                Button(
                    onClick = { viewModel.toggleTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp).testTag("timer_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                OutlinedButton(
                    onClick = { showCustomDurationDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(54.dp).testTag("timer_custom_button")
                ) {
                    Text("Custom", fontSize = 12.sp)
                }
            }
        }

        // Philosophy Reminder Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "StudyBiz Focus Principle:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "50-minute academic blocks maximize recall for Class 9 SSC boards. 30-minute business blocks prevent Shopify from expanding into sleep hours.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Dialog: Custom Duration
    if (showCustomDurationDialog) {
        var customMins by remember { mutableStateOf("45") }

        AlertDialog(
            onDismissRequest = { showCustomDurationDialog = false },
            title = { Text("Set Custom Duration") },
            text = {
                OutlinedTextField(
                    value = customMins,
                    onValueChange = { customMins = it },
                    label = { Text("Minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customMins.toIntOrNull() ?: 25
                        viewModel.selectTimerMode(timerMode, mins)
                        showCustomDurationDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDurationDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Change Topic
    if (showChangeTopicDialog) {
        var newTopic by remember { mutableStateOf(topic) }

        AlertDialog(
            onDismissRequest = { showChangeTopicDialog = false },
            title = { Text("Change Focus Subject / Topic") },
            text = {
                OutlinedTextField(
                    value = newTopic,
                    onValueChange = { newTopic = it },
                    label = { Text("Topic Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTopic.isNotBlank()) {
                            viewModel.setTimerTopic(newTopic.trim())
                            showChangeTopicDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeTopicDialog = false }) { Text("Cancel") }
            }
        )
    }
}
