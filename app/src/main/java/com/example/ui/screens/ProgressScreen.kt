package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.model.GoalEntity
import com.example.ui.components.StatCard
import com.example.ui.theme.BusinessAccent
import com.example.ui.theme.PriorityWarning
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyBizViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: StudyBizViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daily & Weekly Analytics, 1: Goal System
    var showAddGoalDialog by remember { mutableStateOf(false) }

    // Computations
    val todaySessions = focusSessions.filter { it.dateString == viewModel.todayDateString }
    val todayStudyMinutes = todaySessions.filter { it.sessionType == "STUDY" }.sumOf { it.actualDurationMinutes }
    val todayBizMinutes = todaySessions.filter { it.sessionType == "BUSINESS" }.sumOf { it.actualDurationMinutes }

    val todayTasks = tasks.filter { task -> task.dateString == viewModel.todayDateString }
    val todayCompletedTasks = todayTasks.count { it.isCompleted }

    // Weekly metrics
    val weeklyStudyHours = focusSessions.filter { it.sessionType == "STUDY" }.sumOf { it.actualDurationMinutes } / 60.0
    val weeklyBizHours = focusSessions.filter { it.sessionType == "BUSINESS" }.sumOf { it.actualDurationMinutes } / 60.0
    val weeklyStudyTarget = settings.dailyStudyHourTarget * 6.0 // 6 study days
    val weeklyBizTarget = settings.dailyBusinessHourCap * 5.0

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "অ্যানালিটিক্স ও লক্ষ্য ট্র্যাকিং",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "অগ্রগতি ও ধারাবাহিকতা",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "পড়াশোনা ও ব্যবসার সময় ব্যালান্স • এসএসসি লক্ষ্য পূরণ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab Selector
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("সময় বণ্টন ও ট্রেন্ড") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("লক্ষ্য ও মাইলস্টোন (${goals.size})") }
                    )
                }
            }

            if (selectedTab == 0) {
                // Today's Stats Row
                item {
                    Text("আজকের কাজের সারসংক্ষেপ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "পড়ার সময়",
                            value = String.format(Locale.getDefault(), "%.1f ঘণ্টা", todayStudyMinutes / 60.0),
                            subtitle = "লক্ষ্য: ${settings.dailyStudyHourTarget} ঘণ্টা",
                            accentColor = StudyPrimary,
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "ব্যবসার সময়",
                            value = String.format(Locale.getDefault(), "%.1f ঘণ্টা", todayBizMinutes / 60.0),
                            subtitle = "সীমা: ${settings.dailyBusinessHourCap} ঘণ্টা",
                            accentColor = BusinessAccent,
                            icon = Icons.Default.ShoppingBag,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "সম্পন্ন কাজ",
                            value = "$todayCompletedTasks / ${todayTasks.size}",
                            subtitle = if (todayTasks.isNotEmpty()) "${(todayCompletedTasks * 100 / todayTasks.size)}% সম্পন্ন" else "কোনো কাজ নেই",
                            accentColor = PriorityWarning,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "ফোকাস সেশন",
                            value = "${todaySessions.size}",
                            subtitle = "মোট ${todayStudyMinutes + todayBizMinutes} মিনিট",
                            accentColor = Color(0xFFA855F7),
                            icon = Icons.Default.Timer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Weekly Targets vs Actuals Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("weekly_targets_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("সাপ্তাহিক লক্ষ্য বনাম বাস্তব অগ্রগতি", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Academic Target
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("এসএসসি একাডেমিক পড়াশোনা", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${String.format(Locale.getDefault(), "%.1f", weeklyStudyHours)} / ${weeklyStudyTarget.toInt()} ঘণ্টা",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudyPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val studyRatio = (weeklyStudyHours / weeklyStudyTarget).coerceIn(0.0, 1.0).toFloat()
                            LinearProgressIndicator(
                                progress = { studyRatio },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = StudyPrimary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Business Cap
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ড্রপশিপিং ব্যবসার কাজের চাপ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${String.format(Locale.getDefault(), "%.1f", weeklyBizHours)} / ${weeklyBizTarget.toInt()} ঘণ্টা (সর্বোচ্চ)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BusinessAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val bizRatio = (weeklyBizHours / weeklyBizTarget).coerceIn(0.0, 1.0).toFloat()
                            LinearProgressIndicator(
                                progress = { bizRatio },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = BusinessAccent,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }

                // Custom Bar Chart for Weekly Distribution
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("weekly_distribution_chart")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("সপ্তাহের দিনগুলোতে সময় ব্যয় (ঘণ্টা)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(StudyPrimary, CircleShape))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("পড়াশোনা", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(BusinessAccent, CircleShape))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ব্যবসা", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Canvas Bar Chart
                            val days = listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র")
                            val studyValues = listOf(3.5f, 4.0f, 4.5f, 3.8f, 4.2f, 5.0f, 3.0f)
                            val bizValues = listOf(1.0f, 1.2f, 1.5f, 0.8f, 1.0f, 1.4f, 1.0f)
                            val maxVal = 6.0f

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val barWidth = size.width / (days.size * 2.5f)
                                    val spacing = size.width / days.size

                                    for (i in days.indices) {
                                        val x = i * spacing + spacing / 4

                                        // Study Bar
                                        val studyHeight = (studyValues[i] / maxVal) * (size.height - 25f)
                                        drawRoundRect(
                                            color = StudyPrimary,
                                            topLeft = Offset(x, size.height - 25f - studyHeight),
                                            size = Size(barWidth, studyHeight),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )

                                        // Biz Bar
                                        val bizHeight = (bizValues[i] / maxVal) * (size.height - 25f)
                                        drawRoundRect(
                                            color = BusinessAccent,
                                            topLeft = Offset(x + barWidth + 2f, size.height - 25f - bizHeight),
                                            size = Size(barWidth, bizHeight),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    days.forEach { day ->
                                        Text(day, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Goals System Tab
                items(goals, key = { it.id }) { goal ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (goal.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("goal_item_${goal.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = goal.isCompleted,
                                onCheckedChange = { viewModel.toggleGoalCompletion(goal) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (goal.category == "BUSINESS") BusinessAccent else StudyPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = goal.timeframe,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}",
                                        fontSize = 11.sp,
                                        color = if (goal.category == "BUSINESS") BusinessAccent else StudyPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Goal FAB
        if (selectedTab == 1) {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = StudyPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 20.dp)
                    .testTag("add_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    }

    // Dialog: Add Goal
    if (showAddGoalDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var timeframe by remember { mutableStateOf("WEEKLY") }
        var category by remember { mutableStateOf("STUDY") }
        var targetStr by remember { mutableStateOf("5") }
        var unit by remember { mutableStateOf("chapters") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Set New Milestone / Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title (e.g. Study 25 hours)") },
                        modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
                    )

                    // Timeframe picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("DAILY", "WEEKLY", "MONTHLY", "LONG_TERM").forEach { tf ->
                            FilterChip(
                                selected = timeframe == tf,
                                onClick = { timeframe = tf },
                                label = { Text(tf.replace("_", " "), fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Category picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = category == "STUDY",
                            onClick = { category = "STUDY" },
                            label = { Text("SSC Study") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = category == "BUSINESS",
                            onClick = { category = "BUSINESS" },
                            label = { Text("Dropshipping") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = targetStr,
                            onValueChange = { targetStr = it },
                            label = { Text("Target Value") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (hrs/chapters)") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalTitle.isNotBlank()) {
                            viewModel.addGoal(
                                title = goalTitle.trim(),
                                timeframe = timeframe,
                                category = category,
                                target = targetStr.toDoubleOrNull() ?: 1.0,
                                unit = unit.trim()
                            )
                            showAddGoalDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_goal")
                ) {
                    Text("Add Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) { Text("Cancel") }
            }
        )
    }
}
