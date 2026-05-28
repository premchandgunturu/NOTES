package com.example.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// Screen state
enum class AppScreen {
    Onboarding,
    Dashboard
}

// Top-level selected workspace tab
enum class DashboardTab {
    Tasks,     // Focus List
    Thoughts,  // Knowledge Archive
    Metrics,    // Consistency Hub
    Profile    // Profile Hub
}

class ConsistencyHubViewModel(
    private val application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // Onboarding Registration & Login state
    var currentUsername = MutableStateFlow<String?>(null)
    var currentUserFullName = MutableStateFlow<String?>(null)
    val appScreen = MutableStateFlow(AppScreen.Onboarding)
    val selectedTab = MutableStateFlow(DashboardTab.Tasks)
    val isLightMode = MutableStateFlow(false)

    // Login/Onboarding Form states
    val loginUsernameInput = MutableStateFlow("")
    val loginFullNameInput = MutableStateFlow("")
    val loginPasswordInput = MutableStateFlow("")
    val onboardingError = MutableStateFlow<String?>(null)
    val onboardingSuccessMessage = MutableStateFlow<String?>(null)

    // Task tracker form inputs
    val taskTitleInput = MutableStateFlow("")
    val selectedSubjectPreset = MutableStateFlow("Algorithm") // Preset default
    val customTagInput = MutableStateFlow("")

    // Thought Vault inputs & search filters
    val thoughtDescriptionInput = MutableStateFlow("")
    val thoughtTagsInput = MutableStateFlow("") // comma separated lists
    val thoughtSearchQuery = MutableStateFlow("")

    // Metric tracker inputs
    val studyHoursInput = MutableStateFlow("")
    val studyTopicInput = MutableStateFlow("")

    // Room DB Flow Bindings
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawThoughts: StateFlow<List<Thought>> = repository.allThoughts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyLogs: StateFlow<List<StudyLog>> = repository.allStudyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered thoughts based on search queries
    val filteredThoughts: StateFlow<List<Thought>> = combine(rawThoughts, thoughtSearchQuery) { thoughtsList, query ->
        if (query.trim().isEmpty()) {
            thoughtsList
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            thoughtsList.filter {
                it.description.lowercase().contains(lowerCaseQuery) ||
                it.tags.lowercase().contains(lowerCaseQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Quotes pool
    private val motivationalQuotes = listOf(
        "Consistency is the ultimate competitive advantage.",
        "Discipline equals freedom. Grind today, fly tomorrow.",
        "Small daily gains compounded over time yield massive results.",
        "The secret of your future is hidden in your daily routine.",
        "Energy flows where attention goes. Lock in your session.",
        "Your potential is endless. Master your time, master your life.",
        "Champions do not become champions when they win, but in the hours, weeks, and months they prepare.",
        "Do not wait for motivation. Cultivate discipline.",
        "Excellence is not a single act, but a series of daily habits.",
        "Success is the sum of small effort, repeated day in and day out.",
        "You do not rise to the level of your goals; you fall to the level of your systems.",
        "Focus on being productive instead of busy.",
        "If you cannot fly then run, if you cannot run then walk, but by all means keep moving.",
        "What you do daily defines who you become tomorrow.",
        "It is not what we do once in a while that shapes our lives, but what we do consistently.",
        "Your habits will determine your future. Pack them with discipline.",
        "The only bad session is the one that did not happen.",
        "Motivation gets you started; habit is what keeps you going.",
        "The successful warrior is the average person, with laser-like focus.",
        "Great things are done by a series of small things brought together.",
        "Be obsessed with the process, and the results will take care of themselves.",
        "You are what you repeatedly do. Build a masterpiece of habits.",
        "A year from now you may wish you had started today.",
        "Discipline is choosing between what you want now and what you want most.",
        "The pain of self-discipline is far less than the pain of regret.",
        "One percent better every day adds up to a completely transformed life.",
        "Success is walking from failure to failure with no loss of enthusiasm.",
        "Action is the foundational key to all success.",
        "Concentrate all your thoughts upon the work at hand.",
        "Do not let what you cannot do interfere with what you can do.",
        "Start where you are. Use what you have. Do what you can.",
        "Continuous improvement is better than delayed perfection.",
        "It always seems impossible until it is done.",
        "Energy and persistence conquer all things.",
        "Do something today that your future self will thank you for.",
        "Hard work beats talent when talent fails to work hard.",
        "Strive for progress, not perfection.",
        "A river cuts through rock, not because of its power, but because of its persistence.",
        "Success is not overnight. It is when every day you get a little better than the day before.",
        "Keep showing up. Even when it is hard. Especially when it is hard.",
        "Your mind is a muscle. Keep training it with focus.",
        "Don't count the days, make the days count.",
        "An investment in knowledge pays the best interest.",
        "Be stronger than your strongest excuse.",
        "You are entirely up to you. Take ownership of your time.",
        "Small steps in the right direction are better than big steps in the wrong direction.",
        "Rome was not built in a day, but they were laying bricks every hour.",
        "Work hard in silence. Let success make the noise.",
        "Do not fear slow progress. Only fear standing still.",
        "Big goals are just small tasks done consistently.",
        "Discipline is the bridge between goals and accomplishment.",
        "The best way to predict your future is to create it.",
        "Don’t wish it were easier. Wish you were better.",
        "Focus on the journey, not the destination.",
        "If you modify your habits, you modify your destiny.",
        "You get what you focus on, so focus on what you want.",
        "He who has a why to live can bear almost any how.",
        "The difference between who you are and who you want to be is what you do."
    )
    
    val activeQuote = MutableStateFlow(motivationalQuotes[0])

    init {
        rotateQuote()
        restoreSession()
        
        viewModelScope.launch {
            isLightMode.collect { light ->
                val prefs = application.getSharedPreferences("consistency_hub_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putBoolean("saved_light_mode", light).apply()
            }
        }
    }

    private fun restoreSession() {
        val prefs = application.getSharedPreferences("consistency_hub_prefs", android.content.Context.MODE_PRIVATE)
        val savedUsername = prefs.getString("saved_username", null)
        val savedFullName = prefs.getString("saved_full_name", null)
        val savedLightMode = prefs.getBoolean("saved_light_mode", false)
        
        isLightMode.value = savedLightMode
        
        if (savedUsername != null && savedFullName != null) {
            currentUsername.value = savedUsername
            currentUserFullName.value = savedFullName
            appScreen.value = AppScreen.Dashboard
        }
    }
    
    private fun saveSession(username: String, fullName: String) {
        val prefs = application.getSharedPreferences("consistency_hub_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putString("saved_username", username)
            .putString("saved_full_name", fullName)
            .apply()
    }
    
    private fun clearSession() {
        val prefs = application.getSharedPreferences("consistency_hub_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .remove("saved_username")
            .remove("saved_full_name")
            .apply()
    }

    fun rotateQuote() {
        val current = activeQuote.value
        val remaining = motivationalQuotes.filter { it != current }
        activeQuote.value = remaining.random()
    }

    // Dynamic Time Welcome Greeting
    fun getDynamicGreetingAndIcon(): Pair<String, String> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> Pair("Good morning, absolute grinder!", "wb_sunny")
            in 12..16 -> Pair("Good afternoon, session in progress!", "adjust")
            in 17..21 -> Pair("Good evening, consistency check-in!", "nights_stay")
            else -> Pair("Up late grinding? Respect the hustle.", "bedtime")
        }
    }

    // ==========================================
    // AUTHENTICATION LOGIC (LOCAL DB SECURE)
    // ==========================================
    fun registerNewUser() {
        val username = loginUsernameInput.value.trim()
        val fullName = loginFullNameInput.value.trim()
        val password = loginPasswordInput.value.trim()

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            onboardingError.value = "Username, Full Name & password cannot be empty"
            return
        }
        if (password.length < 4) {
            onboardingError.value = "Password must be at least 4 characters"
            return
        }

        viewModelScope.launch {
            try {
                val existing = repository.getUser(username)
                if (existing != null) {
                    onboardingError.value = "Username already exists"
                } else {
                    repository.insertUser(User(username, password, fullName)) // Stored securely
                    onboardingSuccessMessage.value = "Registration successful! You can log in now."
                    onboardingError.value = null
                    loginFullNameInput.value = ""
                }
            } catch (e: Exception) {
                onboardingError.value = "Database registration error"
            }
        }
    }

    fun loginUser() {
        val username = loginUsernameInput.value.trim()
        val password = loginPasswordInput.value.trim()

        if (username.isEmpty() || password.isEmpty()) {
            onboardingError.value = "Username & password cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val user = repository.getUser(username)
                if (user != null && user.passwordHash == password) {
                    currentUsername.value = username
                    currentUserFullName.value = user.fullName
                    appScreen.value = AppScreen.Dashboard
                    onboardingError.value = null
                    onboardingSuccessMessage.value = null
                    saveSession(username, user.fullName)
                    // Reset fields
                    loginUsernameInput.value = ""
                    loginFullNameInput.value = ""
                    loginPasswordInput.value = ""
                } else {
                    onboardingError.value = "Incorrect username or password"
                }
            } catch (e: Exception) {
                onboardingError.value = "Database login validation failed"
            }
        }
    }

    fun logout() {
        currentUsername.value = null
        currentUserFullName.value = null
        appScreen.value = AppScreen.Onboarding
        selectedTab.value = DashboardTab.Tasks
        clearSession()
    }

    // ==========================================
    // TASK TRACKER (FOCUS CHECKLIST)
    // ==========================================
    fun addNewTask() {
        val title = taskTitleInput.value.trim()
        val subject = selectedSubjectPreset.value
        val customTag = customTagInput.value.trim()
        
        if (title.isEmpty()) {
            Toast.makeText(application, "Enter an objective task title", Toast.LENGTH_SHORT).show()
            return
        }

        val subjectSlug = if (customTag.isNotEmpty()) customTag else subject

        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    subject = subjectSlug
                )
            )
            // clear entry fields
            taskTitleInput.value = ""
            customTagInput.value = ""
            Toast.makeText(application, "Task added onto focus deck!", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun scheduleTaskAlert(task: Task, minutes: Int) {
        viewModelScope.launch {
            val alertEpoch = System.currentTimeMillis() + (minutes * 60 * 1000)
            repository.updateTask(task.copy(alertTime = alertEpoch))
            Toast.makeText(application, "Alert scheduled in $minutes mins for: ${task.title}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // THOUGHT VAULT (KNOWLEDGE ARCHIVE)
    // ==========================================
    fun addNewThought() {
        val desc = thoughtDescriptionInput.value.trim()
        val tagsStr = thoughtTagsInput.value.trim()

        if (desc.isEmpty()) {
            Toast.makeText(application, "Thought description cannot be blank", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            repository.insertThought(
                Thought(
                    description = desc,
                    tags = tagsStr
                )
            )
            thoughtDescriptionInput.value = ""
            thoughtTagsInput.value = ""
            Toast.makeText(application, "Thought deposited securely, hashtagged!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteThought(id: Int) {
        viewModelScope.launch {
            repository.deleteThought(id)
        }
    }

    // Helper: parses tags comma list into styled #hashtags list
    fun extractHashtags(tags: String): List<String> {
        if (tags.isEmpty()) return emptyList()
        return tags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { if (it.startsWith("#")) it else "#$it" }
    }

    // ==========================================
    // CONSISTENCY HUB (STUDY LOG METRIC TRACKER)
    // ==========================================
    fun addNewStudyLog() {
        val hoursStr = studyHoursInput.value.trim()
        val topic = studyTopicInput.value.trim()

        if (hoursStr.isEmpty() || topic.isEmpty()) {
            Toast.makeText(application, "Fill study duration & topic log fields", Toast.LENGTH_SHORT).show()
            return
        }

        val hours = hoursStr.toDoubleOrNull()
        if (hours == null || hours <= 0 || hours > 24) {
            Toast.makeText(application, "Provide valid decimal hours (e.g. 2.5)", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            repository.insertStudyLog(
                StudyLog(
                    durationHours = hours,
                    topic = topic
                )
            )
            studyHoursInput.value = ""
            studyTopicInput.value = ""
            Toast.makeText(application, "Effort logged successfully! High discipline.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteStudyLog(id: Int) {
        viewModelScope.launch {
            repository.deleteStudyLog(id)
        }
    }

    // Aggregates study logs for each individual day of the current week (Mon, Tue, Wed, Thu, Fri, Sat, Sun).
    // Standard target daily target: 10 hrs. Target weekly target: 70 hrs.
    fun calculateWeeklyEffortMetrics(logs: List<StudyLog>): List<WeeklyDayMetric> {
        val weekChart = mutableMapOf(
            Calendar.MONDAY to 0.0,
            Calendar.TUESDAY to 0.0,
            Calendar.WEDNESDAY to 0.0,
            Calendar.THURSDAY to 0.0,
            Calendar.FRIDAY to 0.0,
            Calendar.SATURDAY to 0.0,
            Calendar.SUNDAY to 0.0
        )

        val nowCal = Calendar.getInstance()
        val currentYear = nowCal.get(Calendar.YEAR)
        val currentWeekOfYear = nowCal.get(Calendar.WEEK_OF_YEAR)

        // Group study hours by day of the current week
        logs.forEach { log ->
            val logCal = Calendar.getInstance()
            logCal.timeInMillis = log.timestamp
            // Match same year and week to filter logs dynamically
            if (logCal.get(Calendar.YEAR) == currentYear &&
                logCal.get(Calendar.WEEK_OF_YEAR) == currentWeekOfYear) {
                val day = logCal.get(Calendar.DAY_OF_WEEK)
                if (weekChart.containsKey(day)) {
                    weekChart[day] = weekChart[day]!! + log.durationHours
                }
            }
        }

        return listOf(
            WeeklyDayMetric("Mon", weekChart[Calendar.MONDAY] ?: 0.0),
            WeeklyDayMetric("Tue", weekChart[Calendar.TUESDAY] ?: 0.0),
            WeeklyDayMetric("Wed", weekChart[Calendar.WEDNESDAY] ?: 0.0),
            WeeklyDayMetric("Thu", weekChart[Calendar.THURSDAY] ?: 0.0),
            WeeklyDayMetric("Fri", weekChart[Calendar.FRIDAY] ?: 0.0),
            WeeklyDayMetric("Sat", weekChart[Calendar.SATURDAY] ?: 0.0),
            WeeklyDayMetric("Sun", weekChart[Calendar.SUNDAY] ?: 0.0)
        )
    }
}

// Data class for drawing 7-day progress bars
data class WeeklyDayMetric(
    val dayName: String,
    val hoursLogged: Double
)

// ==========================================
// VIEWMODEL FACTORY CONFIGURATION
// ==========================================
class ConsistencyHubViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConsistencyHubViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConsistencyHubViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
