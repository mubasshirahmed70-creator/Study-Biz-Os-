package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BusinessAccent
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyBizViewModel

@Composable
fun SettingsScreen(
    viewModel: StudyBizViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()

    var studentName by remember(settings.studentName) { mutableStateOf(settings.studentName) }
    var apiKey by remember(settings.customGeminiApiKey) { mutableStateOf(settings.customGeminiApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    var studyTargetStr by remember(settings.dailyStudyHourTarget) { mutableStateOf(settings.dailyStudyHourTarget.toString()) }
    var businessCapStr by remember(settings.dailyBusinessHourCap) { mutableStateOf(settings.dailyBusinessHourCap.toString()) }
    var sleepGoalStr by remember(settings.dailySleepHoursGoal) { mutableStateOf(settings.dailySleepHoursGoal.toString()) }
    var studyFocusStr by remember(settings.studyFocusMinutes) { mutableStateOf(settings.studyFocusMinutes.toString()) }
    var studyBreakStr by remember(settings.studyBreakMinutes) { mutableStateOf(settings.studyBreakMinutes.toString()) }
    var bizFocusStr by remember(settings.businessFocusMinutes) { mutableStateOf(settings.businessFocusMinutes.toString()) }
    var bizBreakStr by remember(settings.businessBreakMinutes) { mutableStateOf(settings.businessBreakMinutes.toString()) }

    var showSavedAlert by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "সেটিংস ও প্রোফাইল কনফিগারেশন",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudyPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ব্যক্তিগত স্টাডিবিয সেটিংস",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "টার্গেট, সময়সীমা, রুটিন এবং এআই এপিআই কি পরিবর্তন করুন",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. In-App Gemini API Key Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("api_key_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = BusinessAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Gemini API Key (অ্যাপে ইনপুট)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "এখানে আপনি যেকোনো সময় নিজের Gemini API Key যুক্ত বা পরিবর্তন করতে পারেন। Key সেভ থাকলে অ্যাপের ভয়েস সহকারী সম্পূর্ণ কার্যকর থাকবে।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("settings_api_key_input")
                    )

                    Surface(
                        color = if (apiKey.isNotBlank()) BusinessAccent.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (apiKey.isNotBlank()) "✓ API Key সেট করা আছে" else "⚠ কোনো Key ইনপুট করা নেই (ডিফল্ট মোড চলবে)",
                            color = if (apiKey.isNotBlank()) BusinessAccent else Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 2. Profile Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("শিক্ষার্থী প্রোফাইল", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("শিক্ষার্থীর নাম") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_name_input")
                    )
                    Text("শ্রেণি ও লক্ষ্য: নবম শ্রেণি (SSC বোর্ড প্রস্তুতি)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 3. Fixed Daily Schedule Overview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনার বর্তমান নির্ধারিত সময়সূচি", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("• ঘুম থেকে ওঠা: ফজরের ২.৫ ঘণ্টা আগে (ভোর ৪:০০ টায় গভীর মনোযোগে পড়া)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("• স্কুল সময়: সকাল ৮:০০ টা - বিকাল ৩:৪০ টা (স্কুল শেষে বাড়ি ফেরা)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("• নাইট কোচিং: সন্ধ্যা ৬:০০ টা - রাত ৯:৩০ টা", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("• ঘুমাতে যাওয়া: রাত ১০:৩০ টার মধ্যে (৮ ঘণ্টা ঘুম আবশ্যক)", fontSize = 12.sp, color = BusinessAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Workload & Health Safeguards
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("দৈনিক পড়ার লক্ষ্য ও ড্রপশিপিং সীমা", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = studyTargetStr,
                        onValueChange = { studyTargetStr = it },
                        label = { Text("দৈনিক পড়াশোনার লক্ষ্য (ঘণ্টা) - ১ নম্বর প্রায়োরিটি") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = businessCapStr,
                        onValueChange = { businessCapStr = it },
                        label = { Text("দৈনিক ড্রপশিপিং কাজের সর্বোচ্চ সীমা (ঘণ্টা) - কড়া লিমিট") },
                        supportingText = { Text("পড়াশোনার সময় যেন ব্যবসা দখল না করে সেজন্য সর্বোচ্চ সীমা।") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sleepGoalStr,
                        onValueChange = { sleepGoalStr = it },
                        label = { Text("দৈনিক ঘুমের লক্ষ্য (ঘণ্টা) - সুস্বাস্থ্য রক্ষা") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 5. Focus Presets
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ফোকাস টাইমার সময় নির্ধারণ (মিনিট)", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = studyFocusStr,
                            onValueChange = { studyFocusStr = it },
                            label = { Text("পড়ার ফোকাস") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = studyBreakStr,
                            onValueChange = { studyBreakStr = it },
                            label = { Text("পড়ার বিরতি") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = bizFocusStr,
                            onValueChange = { bizFocusStr = it },
                            label = { Text("ব্যবসা ফোকাস") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bizBreakStr,
                            onValueChange = { bizBreakStr = it },
                            label = { Text("ব্যবসা বিরতি") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 6. Actions: Save & Reset
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        viewModel.updateSettings(
                            studentName = studentName.trim(),
                            studyTarget = studyTargetStr.toDoubleOrNull() ?: 4.5,
                            businessCap = businessCapStr.toDoubleOrNull() ?: 1.5,
                            sleepGoal = sleepGoalStr.toDoubleOrNull() ?: 8.0,
                            studyFocus = studyFocusStr.toIntOrNull() ?: 50,
                            studyBreak = studyBreakStr.toIntOrNull() ?: 10,
                            businessFocus = bizFocusStr.toIntOrNull() ?: 30,
                            businessBreak = bizBreakStr.toIntOrNull() ?: 5,
                            apiKey = apiKey.trim()
                        )
                        showSavedAlert = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_button")
                ) {
                    Text("সেটিংস সংরক্ষণ করুন")
                }

                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reset_data_button")
                ) {
                    Text("রুটিন ও সিলেবাস ডেমো ডেটা রিসেট করুন")
                }
            }
        }
    }

    if (showSavedAlert) {
        AlertDialog(
            onDismissRequest = { showSavedAlert = false },
            title = { Text("সেটিংস সফলভাবে সংরক্ষিত") },
            text = { Text("আপনার স্টাডিবিয ওএস-এর সময়সীমা এবং এপিআই কি সফলভাবে আপডেট হয়েছে।") },
            confirmButton = {
                TextButton(onClick = { showSavedAlert = false }) { Text("ঠিক আছে") }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("ডেমো সিলেবাস ও ডেটা রিসেট করবেন?") },
            text = { Text("এটি ক্লাস ৯ এসএসসি সিলেবাসের বিষয়গুলো ও ড্রপশিপিং টাস্ক পুনরায় ফ্রেশ করে লোড করবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAndReseedData()
                        showResetConfirm = false
                    }
                ) {
                    Text("রিসেট নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("বাতিল") }
            }
        )
    }
}
