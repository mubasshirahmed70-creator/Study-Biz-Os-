package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.StatCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.BusinessAccent
import com.example.ui.theme.PriorityWarning
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyBizViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: StudyBizViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
    val guardNotice by viewModel.priorityGuardNotice.collectAsStateWithLifecycle()
    val isContinuousMode by viewModel.voiceManager.isContinuousMode.collectAsStateWithLifecycle()

    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size
    val dailyProgressRatio = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount else 0f

    val todayStudyMinutes = focusSessions
        .filter { it.dateString == viewModel.todayDateString && it.sessionType == "STUDY" }
        .sumOf { it.actualDurationMinutes }
    val todayStudyHours = todayStudyMinutes / 60.0

    val todayBizMinutes = focusSessions
        .filter { it.dateString == viewModel.todayDateString && it.sessionType == "BUSINESS" }
        .sumOf { it.actualDurationMinutes }
    val todayBizHours = todayBizMinutes / 60.0

    val nextPriorityTask: TaskEntity? = tasks
        .filter { !it.isCompleted }
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
        .firstOrNull()

    val formattedDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("bn-BD")).format(Date())

    // Timeline overview schedule slots for the Class 9 student
    val scheduleSlots = listOf(
        Triple("ভোর ৪:০০ - ৫:১৫", "ফজরপূর্ব নিবিড় গণিত", "একাডেমিক"),
        Triple("সকাল ৮:০০ - ৩:৪০", "স্কুল ও ক্লাস ৯ পাঠ", "স্কুল"),
        Triple("বিকাল ৪:৩০ - ৫:২০", "বিজ্ঞান কুইক রিভিশন", "একাডেমিক"),
        Triple("সন্ধ্যা ৬:০০ - ৯:৩০", "নাইট কোচিং সেশন", "কোচিং"),
        Triple("রাত ৯:৪৫ - ১০:২০", "ড্রপশিপিং রিসার্চ", "ব্যবসা"),
        Triple("রাত ১০:৩০", "ঘুম ও বিশ্রাম (৮ ঘণ্টা)", "স্বাস্থ্য")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Master Greeting Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = formattedDate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudyPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "আসসালামু আলাইকুম, ${settings.studentName} 👋",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ক্লাস ৯ এসএসসি প্রস্তুতি • একাডেমিক সর্বোচ্চ অগ্রাধিকার",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Streak Flame Badge
                        Surface(
                            color = PriorityWarning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.border(1.dp, PriorityWarning.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = "ধারাবাহিকতা",
                                    tint = PriorityWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${settings.streakCount} দিন",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = PriorityWarning
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Priority Guard Warning Banner (Sleep/Academic health protection)
        if (guardNotice != null) {
            item {
                PriorityGuardBanner(message = guardNotice)
            }
        }

        // 3. Ultra-Modern Real-Time Bangla Voice AI Card with Continuous Mode
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.AIAssistant.route) }
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .testTag("home_voice_assistant_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "ভয়েস সহকারী",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "বাংলা এআই ভয়েস সহকারী",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isContinuousMode) Color(0xFF10B981) else Color(0xFF2563EB)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isContinuousMode) "কন্টিনিউয়াস" else "রেডি",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Text(
                                    text = "মুখেই বলুন: \"আজকের রুটিন বানাও\" বা \"ম্যাথ পড়া হয়নি\"",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Button(
                            onClick = { onNavigate(Screen.AIAssistant.route) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("কথা বলুন →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio Waveform Visual Cue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF030712))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(6.dp, 14.dp, 18.dp, 10.dp, 20.dp, 12.dp, 8.dp).forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(3.5.dp)
                                        .height(h)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF38BDF8))
                                )
                            }
                        }
                        Text(
                            text = if (isContinuousMode) "অবিরাম কণ্ঠ আলাপ সক্রিয় • বিরতিহীন কথা বলুন" else "কন্টিনিউয়াস মোড চালু করে অনবরত কথা বলতে পারবেন",
                            fontSize = 10.sp,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                }
            }
        }

        // 4. "এখন কী পড়া উচিত?" (What Should I Do Right Now? Smart Priority Engine)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("what_should_i_do_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "এখন কী পড়া উচিত? (সর্বোচ্চ অগ্রাধিকার)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (nextPriorityTask != null) {
                        Text(
                            text = nextPriorityTask.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PriorityBadge(priority = nextPriorityTask.priority)
                            CategoryPill(category = nextPriorityTask.category)
                            Text(
                                text = "⏱ ${nextPriorityTask.durationMinutes} মিনিট",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.selectTimerMode(
                                    if (nextPriorityTask.category == "BUSINESS") "BUSINESS" else "STUDY",
                                    nextPriorityTask.durationMinutes
                                )
                                viewModel.setTimerTopic(nextPriorityTask.title)
                                onNavigate(Screen.FocusTimer.route)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudyPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("start_focus_action")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ফোকাস টাইমার শুরু করুন (${nextPriorityTask.durationMinutes} মি.)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "🎉 মাশাআল্লাহ! আজকের সমস্ত অগ্রাধিকার পড়া সম্পন্ন হয়েছে!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "এখন একটু বিশ্রাম নিন বা ৩০ মিনিট ড্রপশিপিং নিয়ে গবেষণা করুন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 5. Daily Completion Rate & Priority Balance Gauge
        item {
            Column {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "আজকের টাস্ক সম্পন্ন করার অগ্রগতি",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$completedTasksCount / $totalTasksCount টি কাজ (${(dailyProgressRatio * 100).toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { dailyProgressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = StudyPrimary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "পড়াশোনা হয়েছে",
                        value = String.format(Locale.getDefault(), "%.1f ঘণ্টা", todayStudyHours),
                        subtitle = "টার্গেট: ${settings.dailyStudyHourTarget} ঘণ্টা",
                        accentColor = StudyPrimary,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ব্যবসার সময়",
                        value = String.format(Locale.getDefault(), "%.1f ঘণ্টা", todayBizHours),
                        subtitle = "সর্বোচ্চ সীমা: ${settings.dailyBusinessHourCap} ঘণ্টা",
                        accentColor = BusinessAccent,
                        icon = Icons.Default.ShoppingBag,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 6. Routine Time-Block Snapshot (Class 9 Routine)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "দৈনন্দিন নির্ধারিত সময়সূচি (টাইমটেবিল)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "৮ ঘণ্টা ঘুম",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(scheduleSlots) { (time, name, tag) ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(135.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = time,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudyPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tag,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Today's Tasks & Priorities List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আজকের রুটিন ও পড়ার তালিকা (${tasks.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "প্ল্যানারে যোগ করুন →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudyPrimary,
                    modifier = Modifier
                        .clickable { onNavigate(Screen.Planner.route) }
                        .padding(4.dp)
                )
            }
        }

        items(tasks, key = { it.id }) { task ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleTaskCompletion(task) }
                    .testTag("task_item_${task.id}")
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PriorityBadge(priority = task.priority)
                            CategoryPill(category = task.category)
                            Text(
                                text = "⏱ ${task.durationMinutes} মি.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
