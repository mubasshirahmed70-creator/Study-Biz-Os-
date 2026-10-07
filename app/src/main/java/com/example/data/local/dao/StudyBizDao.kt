package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.BusinessProductEntity
import com.example.data.local.model.ChapterEntity
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.GoalEntity
import com.example.data.local.model.SubjectEntity
import com.example.data.local.model.TaskEntity
import com.example.data.local.model.TestScoreEntity
import com.example.data.local.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyBizDao {

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY isWeak DESC, name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    // Chapters
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY chapterNumber ASC")
    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY needsRevision DESC, isWeak DESC, id ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE isWeak = 1 OR needsRevision = 1")
    fun getWeakOrRevisionChapters(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    // Business Products
    @Query("SELECT * FROM business_products ORDER BY createdAtEpoch DESC")
    fun getAllProducts(): Flow<List<BusinessProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: BusinessProductEntity): Long

    @Update
    suspend fun updateProduct(product: BusinessProductEntity)

    @Delete
    suspend fun deleteProduct(product: BusinessProductEntity)

    // Tasks
    @Query("SELECT * FROM tasks WHERE dateString = :dateString ORDER BY isCompleted ASC, id DESC")
    fun getTasksForDate(dateString: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY dateString DESC, isCompleted ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dateString ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestampEpoch DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE dateString = :dateString")
    fun getFocusSessionsForDate(dateString: String): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    // Test Scores
    @Query("SELECT * FROM test_scores ORDER BY dateString DESC")
    fun getAllTestScores(): Flow<List<TestScoreEntity>>

    @Query("SELECT * FROM test_scores WHERE subjectId = :subjectId ORDER BY dateString DESC")
    fun getTestScoresForSubject(subjectId: Long): Flow<List<TestScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestScore(testScore: TestScoreEntity): Long

    // Goals
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, timeframe ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    // User Settings
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)

    // Bulk delete for reset/seed
    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()
    @Query("DELETE FROM chapters")
    suspend fun clearChapters()
    @Query("DELETE FROM business_products")
    suspend fun clearProducts()
    @Query("DELETE FROM tasks")
    suspend fun clearTasks()
    @Query("DELETE FROM focus_sessions")
    suspend fun clearFocusSessions()
    @Query("DELETE FROM goals")
    suspend fun clearGoals()
}
