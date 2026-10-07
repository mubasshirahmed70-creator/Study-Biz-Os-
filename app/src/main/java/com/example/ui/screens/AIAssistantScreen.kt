package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BusinessAccent
import com.example.ui.theme.PriorityWarning
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyBizViewModel
import kotlin.random.Random

@Composable
fun AIAssistantScreen(
    viewModel: StudyBizViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    val isListening by viewModel.voiceManager.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()
    val isContinuousMode by viewModel.voiceManager.isContinuousMode.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.voiceManager.rmsLevel.collectAsStateWithLifecycle()
    val voiceStatus by viewModel.voiceManager.voiceStatusMessage.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var tempApiKey by remember(userSettings.customGeminiApiKey) { mutableStateOf(userSettings.customGeminiApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Microphone Permission Launcher
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            viewModel.voiceManager.startListening()
        } else {
            Toast.makeText(context, "ভয়েস সহকারীর জন্য মাইক্রোফোন অনুমতি প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Bangla quick categorized prompt chips
    val samplePrompts = listOf(
        "🎯 আজকে রুটিনটা তৈরি করো",
        "⚠️ আজকে গণিত পড়া হয়নি",
        "⏰ স্কুল থেকে ফিরতে একটু দেরি হবে",
        "🎉 কাল স্কুল ছুটি",
        "⏳ হাতে শুধু দুই ঘণ্টা সময় আছে",
        "📦 আজকে ড্রপশিপিং একটু বেশি করতে চাই",
        "📘 ইংরেজি ও বিজ্ঞানের পড়ার সময় দাও",
        "💻 আইসিটি রিভিশন করতে চাই"
    )

    // Pulsing animation for active mic & continuous mode
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.22f else if (isSpeaking) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Sleek Top Console Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(StudyPrimary, BusinessAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "বাংলা এআই পার্সোনাল সহকারী",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (isContinuousMode) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                    Text(
                        text = "ক্লাস ৯ এসএসসি প্রস্তুতি • ড্রপশিপিং ব্যালান্স • রুটিন",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Clear chat button
                IconButton(
                    onClick = { viewModel.clearAiChat() },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "কথোপকথন পরিষ্কার",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // In-App API Key Configuration Button
                Surface(
                    onClick = { showApiKeyDialog = true },
                    color = if (userSettings.customGeminiApiKey.isNotBlank()) BusinessAccent.copy(alpha = 0.15f) else PriorityWarning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("in_app_api_key_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = "API Key",
                            tint = if (userSettings.customGeminiApiKey.isNotBlank()) BusinessAccent else PriorityWarning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userSettings.customGeminiApiKey.isNotBlank()) "Key কানেক্টেড" else "Key সেট করুন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userSettings.customGeminiApiKey.isNotBlank()) BusinessAccent else PriorityWarning
                        )
                    }
                }
            }
        }

        // 2. High-Tech Continuous Conversation Card with Live Audio Spectrum
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isContinuousMode) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = if (isContinuousMode) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isContinuousMode) Color(0xFF2563EB).copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                tint = if (isContinuousMode) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "অবিরাম কণ্ঠ আলাপ (Continuous Conversation)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isContinuousMode) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isContinuousMode) "AI উত্তর শেষ হওয়ার পর একা একাই আপনার কথা শুনবে" else "অনবরত দুই তরফা কথা বলার জন্য অন করুন",
                                fontSize = 10.sp,
                                color = if (isContinuousMode) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isContinuousMode,
                        onCheckedChange = { isChecked ->
                            if (hasMicPermission) {
                                viewModel.voiceManager.setContinuousMode(isChecked)
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB)
                        ),
                        modifier = Modifier.testTag("continuous_mode_switch")
                    )
                }

                // Audio Waveform Spectrum Visualization (Active when listening or speaking)
                AnimatedVisibility(visible = isListening || isSpeaking || isContinuousMode) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isContinuousMode) Color(0xFF030712) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic animated audio wave bars
                                val baseHeights = listOf(8.dp, 16.dp, 22.dp, 14.dp, 24.dp, 12.dp, 18.dp)
                                baseHeights.forEachIndexed { index, defaultH ->
                                    val barH = if (isListening) {
                                        val factor = (rmsLevel * 20f).coerceIn(4f, 22f)
                                        (factor + (index % 3) * 3).dp
                                    } else if (isSpeaking) {
                                        ((index % 4 + 1) * 5).dp
                                    } else {
                                        4.dp
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(barH)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (isListening) Color(0xFFEF4444)
                                                else if (isSpeaking) Color(0xFF10B981)
                                                else Color(0xFF38BDF8).copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }

                            Text(
                                text = voiceStatus ?: if (isListening) "শুনছি... কথা বলুন" else if (isSpeaking) "সহকারী উত্তর দিচ্ছে..." else "অবিরাম মোড সক্রিয়",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isListening) Color(0xFFFCA5A5) else if (isSpeaking) Color(0xFF86EFAC) else Color(0xFF93C5FD)
                            )

                            if (isSpeaking) {
                                Surface(
                                    onClick = { viewModel.voiceManager.stopSpeaking() },
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("থামুন", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Suggestion Prompt Chips in Bengali
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(samplePrompts) { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { viewModel.sendAiPrompt(prompt, speakAloud = true) },
                    label = { Text(prompt, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // 4. Conversation Messages Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(StudyPrimary, BusinessAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) Color(0xFF1E3A8A) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            modifier = Modifier.testTag("ai_message_${msg.sender}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Normal
                                )

                                // Action bar for assistant response (Play Voice / Copy)
                                if (!isUser) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            onClick = { viewModel.voiceManager.speak(msg.text) },
                                            color = Color.Transparent
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "শুনুন",
                                                    tint = StudyPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("শুনুন", fontSize = 11.sp, color = StudyPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Surface(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("AI Text", msg.text))
                                                Toast.makeText(context, "টেক্সট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            },
                                            color = Color.Transparent
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "কপি",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("কপি", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = StudyPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "আপনার ক্লাস ৯ সিলেবাস ও রুটিন মিলিয়ে চিন্তা করছি...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 5. Ergonomic Voice Orb & Text Input Row (Fixed above bottom navigation)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 85.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real-Time Voice Mic Button with Pulsing Wave Halo
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) Brush.radialGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
                        else if (isSpeaking) Brush.radialGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                        else if (isContinuousMode) Brush.radialGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
                        else Brush.linearGradient(listOf(StudyPrimary, BusinessAccent))
                    )
                    .clickable {
                        if (hasMicPermission) {
                            if (isListening) {
                                viewModel.voiceManager.stopListening()
                            } else {
                                viewModel.voiceManager.startListening()
                            }
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                    .testTag("voice_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "কথা বলুন",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("মুখে বলুন বা বাংলায় লিখুন...", fontSize = 12.sp) },
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        viewModel.sendAiPrompt(text, speakAloud = isContinuousMode)
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(BusinessAccent)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "পাঠান",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    // In-App API Key Configuration Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = StudyPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini API Key ইনপুট ও কনফিগ")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "আপনি সরাসরি এখানে আপনার Google Gemini API Key দিতে পারেন। এটি আপনার ফোনে নিরাপদে থাকবে এবং তাৎক্ষণিকভাবে এআই সহকারীকে আরও শক্তিশালী করে তুলবে।",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_dialog_input")
                    )

                    if (userSettings.customGeminiApiKey.isNotBlank()) {
                        TextButton(
                            onClick = {
                                tempApiKey = ""
                                viewModel.saveInAppApiKey("")
                            }
                        ) {
                            Text("Key মুছে ফেলুন", color = Color(0xFFEF4444), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveInAppApiKey(tempApiKey)
                        showApiKeyDialog = false
                    },
                    modifier = Modifier.testTag("save_api_key_confirm_btn")
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
