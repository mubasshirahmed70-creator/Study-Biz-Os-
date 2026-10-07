package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.model.TaskEntity
import com.example.ui.components.CategoryPill
import com.example.ui.components.PriorityBadge
import com.example.ui.components.PriorityGuardBanner
import com.example.ui.theme.BusinessAccent
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyBizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: StudyBizViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val guardNotice by viewModel.priorityGuardNotice.collectAsStateWithLifecycle()

    var viewMode by remember { mutableIntStateOf(0) } // 0: Today's Schedule, 1: Weekly Overview
    var categoryFilter by remember { mutableStateOf("ALL") } // ALL, STUDY, BUSINESS
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Filter tasks
    val filteredTasks = tasks
        .filter { task ->
            if (viewMode == 0) task.dateString == viewModel.todayDateString else true
        }
        .filter { task ->
            if (categoryFilter == "ALL") true else task.category == categoryFilter
        }
        .sortedBy { task ->
            when (task.priority) {
                "EXAM_PREP" -> 1
                "ACADEMIC_DEADLINE" -> 2
                "WEAK_ACADEMIC" -> 3
                "DAILY_STUDY_TARGET" -> 4
                "IMPORTANT_BUSINESS" -> 5
                "OPTIONAL_BUSINESS" -> 6
                else -> 7
            }
        }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "সময় ও অগ্রাধিকার প্ল্যানার",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (viewMode == 0) "আজকের টাইম-ব্লক ও রুটিন" else "সাপ্তাহিক স্টাডি ও বিজনেস প্ল্যান",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "কড়া প্রায়োরিটি: একাডেমিক পড়াশোনা #১ • ড্রপশিপিং #২",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Priority Guard Banner
            if (guardNotice != null) {
                item {
                    PriorityGuardBanner(message = guardNotice)
                }
            }

            // View Mode Tab
            item {
                TabRow(
                    selectedTabIndex = viewMode,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        text = { Text("আজকের টাইম-ব্লক") }
                    )
                    Tab(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        text = { Text("সাপ্তাহিক পরিকল্পনা") }
                    )
                }
            }

            // Category Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = categoryFilter == "ALL",
                        onClick = { categoryFilter = "ALL" },
                        label = { Text("সকল কাজ (${tasks.size})") }
                    )
                    FilterChip(
                        selected = categoryFilter == "STUDY",
                        onClick = { categoryFilter = "STUDY" },
                        label = { Text("পড়াশোনা (${tasks.count { it.category == "STUDY" }})") }
                    )
                    FilterChip(
                        selected = categoryFilter == "BUSINESS",
                        onClick = { categoryFilter = "BUSINESS" },
                        label = { Text("ড্রপশিপিং (${tasks.count { it.category == "BUSINESS" }})") }
                    )
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No tasks scheduled.", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap '+' to schedule a study block or business task.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleTaskCompletion(task) }
                            .testTag("planner_task_${task.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (task.category == "BUSINESS") BusinessAccent else StudyPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PriorityBadge(priority = task.priority)
                                    CategoryPill(category = task.category)
                                    if (task.timeBlock != null) {
                                        Text("📅 ${task.timeBlock}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("⏱ ${task.durationMinutes}m", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteTask(task) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Add Task FAB
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = StudyPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_task_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    // Dialog: Add Task with Priority Picker
    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("STUDY") }
        var priority by remember { mutableStateOf("DAILY_STUDY_TARGET") }
        var durationStr by remember { mutableStateOf("45") }
        var timeBlock by remember { mutableStateOf("") }
        var subjectOrArea by remember { mutableStateOf("Mathematics") }
        var notes by remember { mutableStateOf("") }

        val priorities = listOf(
            Pair("EXAM_PREP", "1 • Exam Preparation"),
            Pair("ACADEMIC_DEADLINE", "2 • Academic Deadline"),
            Pair("WEAK_ACADEMIC", "3 • Weak Subject Topic"),
            Pair("DAILY_STUDY_TARGET", "4 • Daily Study Target"),
            Pair("IMPORTANT_BUSINESS", "5 • Important Business"),
            Pair("OPTIONAL_BUSINESS", "6 • Optional Business")
        )

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Schedule New Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                    )

                    // Category Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = category == "STUDY",
                            onClick = {
                                category = "STUDY"
                                priority = "DAILY_STUDY_TARGET"
                            },
                            label = { Text("SSC Study") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = category == "BUSINESS",
                            onClick = {
                                category = "BUSINESS"
                                priority = "IMPORTANT_BUSINESS"
                            },
                            label = { Text("Dropshipping") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Priority Selector
                    Text("Priority Hierarchy:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(priorities) { (key, label) ->
                            FilterChip(
                                selected = priority == key,
                                onClick = { priority = key },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Duration (min)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = timeBlock,
                            onValueChange = { timeBlock = it },
                            label = { Text("Time Block (optional)") },
                            placeholder = { Text("e.g. 5:00-6:00 PM") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addTask(
                                title = title.trim(),
                                category = category,
                                priority = priority,
                                durationMinutes = durationStr.toIntOrNull() ?: 45,
                                timeBlock = if (timeBlock.isNotBlank()) timeBlock.trim() else null,
                                subjectOrArea = subjectOrArea,
                                notes = notes.trim()
                            )
                            showAddTaskDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_task")
                ) {
                    Text("Schedule Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
            }
        )
    }
}
