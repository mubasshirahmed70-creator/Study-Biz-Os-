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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.model.ChapterEntity
import com.example.data.local.model.SubjectEntity
import com.example.ui.theme.PriorityExam
import com.example.ui.theme.PriorityWarning
import com.example.ui.theme.PriorityWeak
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyBizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: StudyBizViewModel,
    onNavigateToTimer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Subjects, 1: Weak & Revision
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddChapterDialogForSubjectId by remember { mutableStateOf<Long?>(null) }
    var selectedChapterForEdit by remember { mutableStateOf<ChapterEntity?>(null) }

    // Overall syllabus metrics
    val totalChapters = chapters.size
    val completedChapters = chapters.count { it.status == "COMPLETED" }
    val inProgressChapters = chapters.count { it.status == "IN_PROGRESS" }
    val overallPercent = if (totalChapters > 0) (completedChapters * 100) / totalChapters else 0

    val weakChapters = chapters.filter { it.isWeak }
    val revisionChapters = chapters.filter { it.needsRevision }

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
                        text = "একাডেমিক পড়াশোনা মডিউল",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "নবম শ্রেণি এসএসসি সিলেবাস ট্র্যাকার",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "সর্বোচ্চ অগ্রাধিকার প্রস্তুতি • অধ্যায়ভিত্তিক প্রস্তুতি সম্পন্ন করুন",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Overall Syllabus Progress Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("syllabus_progress_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সার্বিক সিলেবাস সম্পন্নতার হার",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$overallPercent%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StudyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { overallPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = StudyPrimary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✓ $completedChapters সম্পন্ন",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "⏳ $inProgressChapters চলছে",
                                fontSize = 11.sp,
                                color = PriorityWarning,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "⚠ ${weakChapters.size} দুর্বল টপিক",
                                fontSize = 11.sp,
                                color = PriorityWeak,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
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
                        text = { Text("সকল বিষয় (${subjects.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("দুর্বল ও রিভিশন (${weakChapters.size + revisionChapters.size})") }
                    )
                }
            }

            // Tab Content
            if (selectedTab == 0) {
                // Show subjects list with chapters
                items(subjects, key = { it.id }) { subject ->
                    val subjectChapters = chapters.filter { it.subjectId == subject.id }
                    SubjectAccordionCard(
                        subject = subject,
                        chapters = subjectChapters,
                        onAddChapter = { showAddChapterDialogForSubjectId = subject.id },
                        onEditChapter = { selectedChapterForEdit = it },
                        onStartTimer = { title -> onNavigateToTimer(title) }
                    )
                }
            } else {
                // Weak & Revision Focus
                item {
                    Text(
                        text = "Prioritize these topics before tackling dropshipping:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val criticalList = (weakChapters + revisionChapters).distinctBy { it.id }
                if (criticalList.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Text(
                                text = "✨ Excellent! No topics marked weak or needing urgent revision.",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(criticalList, key = { it.id }) { chapter ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { selectedChapterForEdit = chapter }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = chapter.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (chapter.isWeak) {
                                            Surface(
                                                color = PriorityWeak.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Weak Topic", color = PriorityWeak, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        if (chapter.needsRevision) {
                                            Surface(
                                                color = PriorityWarning.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Needs Revision", color = PriorityWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Text("${chapter.completionPercent}% Complete", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Button(
                                    onClick = { onNavigateToTimer(chapter.title) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Study", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to add new subject
        FloatingActionButton(
            onClick = { showAddSubjectDialog = true },
            containerColor = StudyPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_subject_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Subject")
        }
    }

    // Dialog: Add Subject
    if (showAddSubjectDialog) {
        var subjectName by remember { mutableStateOf("") }
        var totalChaps by remember { mutableStateOf("10") }
        var isSubjectWeak by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add New Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name (e.g. Sanskrit, Biology)") },
                        modifier = Modifier.fillMaxWidth().testTag("subject_name_input")
                    )
                    OutlinedTextField(
                        value = totalChaps,
                        onValueChange = { totalChaps = it },
                        label = { Text("Total Chapters") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = isSubjectWeak,
                            onClick = { isSubjectWeak = !isSubjectWeak },
                            label = { Text(if (isSubjectWeak) "Marked as Weak Subject" else "Standard Difficulty") },
                            leadingIcon = {
                                Icon(if (isSubjectWeak) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null)
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            viewModel.addSubject(
                                name = subjectName.trim(),
                                colorHex = "#3B82F6",
                                totalChapters = totalChaps.toIntOrNull() ?: 10,
                                isWeak = isSubjectWeak
                            )
                            showAddSubjectDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_subject")
                ) {
                    Text("Add Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Add Chapter to Subject
    showAddChapterDialogForSubjectId?.let { subjectId ->
        var chapterTitle by remember { mutableStateOf("") }
        var chapterNum by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { showAddChapterDialogForSubjectId = null },
            title = { Text("Add Chapter / Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = chapterTitle,
                        onValueChange = { chapterTitle = it },
                        label = { Text("Chapter Title (e.g. Lines and Angles)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chapterNum,
                        onValueChange = { chapterNum = it },
                        label = { Text("Chapter Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chapterTitle.isNotBlank()) {
                            viewModel.addChapter(
                                subjectId = subjectId,
                                title = chapterTitle.trim(),
                                number = chapterNum.toIntOrNull() ?: 1
                            )
                            showAddChapterDialogForSubjectId = null
                        }
                    }
                ) {
                    Text("Add Chapter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialogForSubjectId = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Edit Chapter Status & Details
    selectedChapterForEdit?.let { chapter ->
        var currentStatus by remember { mutableStateOf(chapter.status) }
        var completionPercent by remember { mutableIntStateOf(chapter.completionPercent) }
        var isWeak by remember { mutableStateOf(chapter.isWeak) }
        var needsRevision by remember { mutableStateOf(chapter.needsRevision) }

        AlertDialog(
            onDismissRequest = { selectedChapterForEdit = null },
            title = { Text(chapter.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("NOT_STARTED", "IN_PROGRESS", "COMPLETED", "NEED_REVISION").forEach { status ->
                            val label = when (status) {
                                "NOT_STARTED" -> "Not Started"
                                "IN_PROGRESS" -> "In Progress"
                                "COMPLETED" -> "Completed"
                                else -> "Revision"
                            }
                            FilterChip(
                                selected = currentStatus == status,
                                onClick = {
                                    currentStatus = status
                                    if (status == "COMPLETED") completionPercent = 100
                                    if (status == "NOT_STARTED") completionPercent = 0
                                },
                                label = { Text(label, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text("Completion: $completionPercent%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = completionPercent.toFloat(),
                        onValueChange = { completionPercent = it.toInt() },
                        valueRange = 0f..100f
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isWeak,
                            onClick = { isWeak = !isWeak },
                            label = { Text("Weak Topic") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PriorityWeak.copy(alpha = 0.2f),
                                selectedLabelColor = PriorityWeak
                            )
                        )
                        FilterChip(
                            selected = needsRevision,
                            onClick = { needsRevision = !needsRevision },
                            label = { Text("Need Revision") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PriorityWarning.copy(alpha = 0.2f),
                                selectedLabelColor = PriorityWarning
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateChapterStatus(chapter, currentStatus, completionPercent)
                        if (isWeak != chapter.isWeak) viewModel.toggleChapterWeak(chapter)
                        if (needsRevision != chapter.needsRevision) viewModel.toggleChapterRevision(chapter)
                        selectedChapterForEdit = null
                    }
                ) {
                    Text("Save Updates")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedChapterForEdit = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun SubjectAccordionCard(
    subject: SubjectEntity,
    chapters: List<ChapterEntity>,
    onAddChapter: () -> Unit,
    onEditChapter: (ChapterEntity) -> Unit,
    onStartTimer: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val completedCount = chapters.count { it.status == "COMPLETED" }
    val percent = if (chapters.isNotEmpty()) (completedCount * 100) / chapters.size else 0

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(subject.colorHex)).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color(android.graphics.Color.parseColor(subject.colorHex)),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = subject.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (subject.isWeak) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = PriorityWeak.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Weak Subject", color = PriorityWeak, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text(
                            text = "$completedCount / ${chapters.size} chapters ($percent%)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(android.graphics.Color.parseColor(subject.colorHex)),
                trackColor = MaterialTheme.colorScheme.surface
            )

            // Expanded chapter details
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    chapters.forEach { chapter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditChapter(chapter) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ch ${chapter.chapterNumber}: ${chapter.title}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val statusColor = when (chapter.status) {
                                        "COMPLETED" -> Color(0xFF10B981)
                                        "IN_PROGRESS" -> PriorityWarning
                                        "NEED_REVISION" -> Color(0xFFF97316)
                                        else -> Color.Gray
                                    }
                                    Text(
                                        text = chapter.status.replace("_", " "),
                                        fontSize = 10.sp,
                                        color = statusColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text("• ${chapter.completionPercent}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (chapter.isWeak) Text("• Weak", fontSize = 10.sp, color = PriorityWeak, fontWeight = FontWeight.Bold)
                                    if (chapter.needsRevision) Text("• Revise", fontSize = 10.sp, color = PriorityWarning, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { onStartTimer("${subject.name} - ${chapter.title}") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = "Focus", tint = StudyPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onAddChapter,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Chapter to ${subject.name}", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
