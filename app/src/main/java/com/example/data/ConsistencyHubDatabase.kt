package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. DATABASE ENTITIES (SQLite Tables)
// ==========================================

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val fullName: String = ""
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String, // e.g., "Admin", "Algorithm", "Formula", "Meeting", "Chore"
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val alertTime: Long? = null
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val tags: String, // Comma-separated tags
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 0, // 0: Low, 1: Medium, 2: High
    val backgroundUri: String? = null,
    val filePath: String? = null
)

@Entity(tableName = "thoughts")
data class Thought(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val tags: String, // Comma-separated tags
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_logs")
data class StudyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationHours: Double,
    val topic: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 2. DATA ACCESS OBJECT (DAO)
// ==========================================

@Dao
interface AppDao {
    // User credentials
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Task list queries
    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    // Thought/Knowledge Archive queries
    @Query("SELECT * FROM thoughts ORDER BY timestamp DESC")
    fun getAllThoughts(): Flow<List<Thought>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThought(thought: Thought)

    @Query("DELETE FROM thoughts WHERE id = :thoughtId")
    suspend fun deleteThought(thoughtId: Int)

    // Note queries
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Int)

    // Study Hour logs queries
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC")
    fun getAllStudyLogs(): Flow<List<StudyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyLog(studyLog: StudyLog)

    @Query("DELETE FROM study_logs WHERE id = :logId")
    suspend fun deleteStudyLog(logId: Int)
}

// ==========================================
// 3. DATABASE CONTEXT
// ==========================================

@Database(
    entities = [User::class, Task::class, Thought::class, Note::class, StudyLog::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "consistency_hub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ==========================================
// 4. REPOSITORY (MANDATORY PATTERN)
// ==========================================

class AppRepository(private val appDao: AppDao) {

    suspend fun getUser(username: String): User? = appDao.getUser(username)

    suspend fun insertUser(user: User) = appDao.insertUser(user)

    val allTasks: Flow<List<Task>> = appDao.getAllTasks()

    suspend fun insertTask(task: Task) = appDao.insertTask(task)

    suspend fun updateTask(task: Task) = appDao.updateTask(task)

    suspend fun deleteTask(taskId: Int) = appDao.deleteTask(taskId)

    val allThoughts: Flow<List<Thought>> = appDao.getAllThoughts()

    suspend fun insertThought(thought: Thought) = appDao.insertThought(thought)

    suspend fun deleteThought(thoughtId: Int) = appDao.deleteThought(thoughtId)

    val allNotes: Flow<List<Note>> = appDao.getAllNotes()

    suspend fun insertNote(note: Note) = appDao.insertNote(note)

    suspend fun updateNote(note: Note) = appDao.updateNote(note)

    suspend fun deleteNote(noteId: Int) = appDao.deleteNote(noteId)

    val allStudyLogs: Flow<List<StudyLog>> = appDao.getAllStudyLogs()

    suspend fun insertStudyLog(studyLog: StudyLog) = appDao.insertStudyLog(studyLog)

    suspend fun deleteStudyLog(logId: Int) = appDao.deleteStudyLog(logId)
}
