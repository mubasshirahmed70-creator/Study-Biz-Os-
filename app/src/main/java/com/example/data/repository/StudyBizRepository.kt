package com.example.data.repository

import com.example.data.local.dao.StudyBizDao
import com.example.data.local.model.BusinessProductEntity
import com.example.data.local.model.ChapterEntity
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.GoalEntity
import com.example.data.local.model.SubjectEntity
import com.example.data.local.model.TaskEntity
import com.example.data.local.model.TestScoreEntity
import com.example.data.local.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

class StudyBizRepository(private val dao: StudyBizDao) {

    // Subjects
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    suspend fun getSubjectById(id: Long) = dao.getSubjectById(id)
    suspend fun insertSubject(subject: SubjectEntity) = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    // Chapters
    val allChapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    fun getChaptersForSubject(subjectId: Long) = dao.getChaptersForSubject(subjectId)
    val weakOrRevisionChapters: Flow<List<ChapterEntity>> = dao.getWeakOrRevisionChapters()
    suspend fun insertChapter(chapter: ChapterEntity) = dao.insertChapter(chapter)
    suspend fun updateChapter(chapter: ChapterEntity) = dao.updateChapter(chapter)
    suspend fun deleteChapter(chapter: ChapterEntity) = dao.deleteChapter(chapter)

    // Products
    val allProducts: Flow<List<BusinessProductEntity>> = dao.getAllProducts()
    suspend fun insertProduct(product: BusinessProductEntity) = dao.insertProduct(product)
    suspend fun updateProduct(product: BusinessProductEntity) = dao.updateProduct(product)
    suspend fun deleteProduct(product: BusinessProductEntity) = dao.deleteProduct(product)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = dao.getPendingTasks()
    fun getTasksForDate(dateString: String) = dao.getTasksForDate(dateString)
    suspend fun insertTask(task: TaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)

    // Focus Sessions
    val allFocusSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()
    fun getFocusSessionsForDate(dateString: String) = dao.getFocusSessionsForDate(dateString)
    suspend fun logFocusSession(session: FocusSessionEntity) = dao.insertFocusSession(session)

    // Test Scores
    val allTestScores: Flow<List<TestScoreEntity>> = dao.getAllTestScores()
    suspend fun insertTestScore(testScore: TestScoreEntity) = dao.insertTestScore(testScore)

    // Goals
    val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()
    suspend fun insertGoal(goal: GoalEntity) = dao.insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    // Settings
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()
    suspend fun saveUserSettings(settings: UserSettingsEntity) = dao.saveUserSettings(settings)

    // Reset database to fresh seed
    suspend fun resetToSeed() {
        dao.clearSubjects()
        dao.clearChapters()
        dao.clearProducts()
        dao.clearTasks()
        dao.clearFocusSessions()
        dao.clearGoals()
    }
}
