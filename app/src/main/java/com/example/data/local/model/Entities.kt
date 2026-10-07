package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "SSC_ACADEMIC", // SSC Academic
    val colorHex: String = "#3B82F6",
    val totalChapters: Int = 12,
    val completedChapters: Int = 0,
    val isWeak: Boolean = false,
    val targetScore: Int = 90
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val chapterNumber: Int = 1,
    val status: String = "NOT_STARTED", // NOT_STARTED, IN_PROGRESS, COMPLETED, NEED_REVISION
    val completionPercent: Int = 0, // 0 - 100
    val isWeak: Boolean = false,
    val needsRevision: Boolean = false,
    val notes: String = "",
    val lastStudiedEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_products")
data class BusinessProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val supplierName: String = "AliExpress",
    val supplierUrl: String = "",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val estimatedMargin: Double = 0.0,
    val status: String = "RESEARCHING", // RESEARCHING, TESTING, READY_TO_LAUNCH, WINNER, DROPPED
    val category: String = "Gadgets",
    val notes: String = "",
    val createdAtEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "STUDY", // STUDY, BUSINESS, HEALTH
    val priority: String = "DAILY_STUDY_TARGET",
    // Hierarchy:
    // 1: EXAM_PREP
    // 2: ACADEMIC_DEADLINE
    // 3: WEAK_ACADEMIC
    // 4: DAILY_STUDY_TARGET
    // 5: IMPORTANT_BUSINESS
    // 6: OPTIONAL_BUSINESS
    val durationMinutes: Int = 45,
    val dateString: String, // YYYY-MM-DD
    val timeBlock: String? = null, // e.g. "04:00 PM - 05:00 PM"
    val isCompleted: Boolean = false,
    val completedAtEpoch: Long? = null,
    val subjectOrArea: String = "General",
    val notes: String = ""
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionType: String = "STUDY", // STUDY, BUSINESS
    val targetDurationMinutes: Int = 50,
    val actualDurationMinutes: Int = 50,
    val dateString: String, // YYYY-MM-DD
    val timestampEpoch: Long = System.currentTimeMillis(),
    val associatedTopic: String = "Study",
    val completedSuccessfully: Boolean = true
)

@Entity(tableName = "test_scores")
data class TestScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val testName: String,
    val scoreObtained: Double,
    val totalMarks: Double = 100.0,
    val dateString: String,
    val notes: String = ""
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timeframe: String = "WEEKLY", // DAILY, WEEKLY, MONTHLY, LONG_TERM
    val category: String = "STUDY", // STUDY, BUSINESS, HEALTH
    val targetValue: Double = 1.0,
    val currentValue: Double = 0.0,
    val unit: String = "tasks",
    val isCompleted: Boolean = false,
    val deadlineDateString: String? = null
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val studentName: String = "মুবাশ্বির",
    val gradeLevel: String = "নবম শ্রেণি (SSC প্রস্তুতি)",
    val dailyStudyHourTarget: Double = 4.5,
    val dailyBusinessHourCap: Double = 1.5,
    val dailySleepHoursGoal: Double = 8.0,
    val studyFocusMinutes: Int = 50,
    val studyBreakMinutes: Int = 10,
    val businessFocusMinutes: Int = 30,
    val businessBreakMinutes: Int = 5,
    val streakCount: Int = 7,
    val customGeminiApiKey: String = "",
    val schoolStartTime: String = "08:00 AM",
    val schoolEndTime: String = "03:40 PM",
    val coachingStartTime: String = "06:00 PM",
    val coachingEndTime: String = "09:30 PM",
    val wakeUpBeforeFajrHours: Double = 2.5
)
