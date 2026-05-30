package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ProblemEntity
import com.example.data.database.SubmissionEntity
import com.example.data.database.UserProfileEntity
import com.example.data.network.AIEvaluationResult
import com.example.data.network.AIInsightsResult
import com.example.data.network.GeminiApiClient
import com.example.data.repository.CodePracticeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class SolveReward(
    val basePoints: Int,
    val speedBonus: Int,
    val streakBonus: Int,
    val totalEarned: Int,
    val newBadges: List<String>
)

class CodePracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CodePracticeRepository
    
    // --- Room flows ---
    val userProfile: StateFlow<UserProfileEntity?>
    val allProblems: StateFlow<List<ProblemEntity>>
    val allSubmissions: StateFlow<List<SubmissionEntity>>

    // --- Search & Filtering States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("All") // All, Easy, Medium, Hard
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    val filteredProblems: StateFlow<List<ProblemEntity>>

    // --- Active Workspace Custom States ---
    private val _selectedProblem = MutableStateFlow<ProblemEntity?>(null)
    val selectedProblem: StateFlow<ProblemEntity?> = _selectedProblem.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Kotlin") // Kotlin, Python, Java
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _enteredCode = MutableStateFlow("")
    val enteredCode: StateFlow<String> = _enteredCode.asStateFlow()

    private val _isEvaluating = MutableStateFlow(false)
    val isEvaluating: StateFlow<Boolean> = _isEvaluating.asStateFlow()

    private val _evaluationResult = MutableStateFlow<AIEvaluationResult?>(null)
    val evaluationResult: StateFlow<AIEvaluationResult?> = _evaluationResult.asStateFlow()

    // Submissions exclusive to the active problem in the editor workspace
    val currentProblemSubmissions: StateFlow<List<SubmissionEntity>>

    // --- Judge0 Configuration ---
    val judge0Host = MutableStateFlow("") // Empty triggers fallback AI
    val judge0Key = MutableStateFlow("")

    // --- AI Smart Insights & Social Integrations ---
    private val _isGeneratingPost = MutableStateFlow(false)
    val isGeneratingPost: StateFlow<Boolean> = _isGeneratingPost.asStateFlow()

    private val _linkedInPostText = MutableStateFlow("")
    val linkedInPostText: StateFlow<String> = _linkedInPostText.asStateFlow()

    private val _isGeneratingInsights = MutableStateFlow(false)
    val isGeneratingInsights: StateFlow<Boolean> = _isGeneratingInsights.asStateFlow()

    private val _aiInsights = MutableStateFlow<AIInsightsResult?>(null)
    val aiInsights: StateFlow<AIInsightsResult?> = _aiInsights.asStateFlow()

    // --- Developer Gamification & Timing Systems ---
    private val _solveTimerSeconds = MutableStateFlow(0)
    val solveTimerSeconds: StateFlow<Int> = _solveTimerSeconds.asStateFlow()

    private val _solveReward = MutableStateFlow<SolveReward?>(null)
    val solveReward: StateFlow<SolveReward?> = _solveReward.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    private fun startTimer() {
        timerJob?.cancel()
        _solveTimerSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _solveTimerSeconds.value++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun clearSolveReward() {
        _solveReward.value = null
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CodePracticeRepository(database.historyDao())

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allProblems = repository.allProblems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSubmissions = repository.allSubmissions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentProblemSubmissions = selectedProblem
            .flatMapLatest { problem ->
                if (problem == null) flowOf(emptyList())
                else repository.getSubmissionsForProblem(problem.id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Filter and Search problem pipeline
        filteredProblems = combine(allProblems, searchQuery, selectedDifficulty) { problems, query, difficulty ->
            problems.filter { problem ->
                val matchesQuery = problem.title.contains(query, ignoreCase = true) ||
                        problem.tags.contains(query, ignoreCase = true)
                val matchesDiff = difficulty == "All" || problem.difficulty.equals(difficulty, ignoreCase = true)
                matchesQuery && matchesDiff
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Ensure database template seed and load
        viewModelScope.launch {
            repository.verifyAndSeedDatabase()
        }
    }

    // --- State setters ---

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDifficultyFilter(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun selectProblem(problem: ProblemEntity?) {
        _selectedProblem.value = problem
        _evaluationResult.value = null
        if (problem != null) {
            // Preload respective language template code
            updateEditorTemplate(problem, _selectedLanguage.value)
            startTimer()
        } else {
            _enteredCode.value = ""
            stopTimer()
        }
    }

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
        _selectedProblem.value?.let {
            updateEditorTemplate(it, language)
        }
    }

    fun onCodeEdited(newCode: String) {
        _enteredCode.value = newCode
    }

    private fun updateEditorTemplate(problem: ProblemEntity, language: String) {
        _enteredCode.value = when (language) {
            "Kotlin" -> problem.templateKotlin
            "Python" -> problem.templatePython
            "Java" -> problem.templateJava
            else -> "// Starter workspace template"
        }
    }

    fun resetWorkspace() {
        _selectedProblem.value?.let {
            updateEditorTemplate(it, _selectedLanguage.value)
        }
        _evaluationResult.value = null
    }

    // --- Form / Setting Actions ---

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value ?: UserProfileEntity(name = name, email = email)
            repository.saveUserProfile(current.copy(name = name, email = email))
        }
    }

    // --- Compile & Evaluate Submissions ---

    fun submitCode() {
        val problem = selectedProblem.value ?: return
        val code = enteredCode.value
        val lang = selectedLanguage.value

        _isEvaluating.value = true
        _evaluationResult.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.evaluateSubmission(
                problem = problem,
                code = code,
                language = lang,
                judge0Host = judge0Host.value,
                judge0Key = judge0Key.value
            )

            _evaluationResult.value = result
            _isEvaluating.value = false

            // Capture attempt and save submission in Room
            val isSuccess = result.status_id == 3
            val submissionResultString = when (result.status_id) {
                3 -> "Accepted"
                4 -> "Wrong Answer"
                6 -> "Compile Error"
                else -> "Runtime Error"
            }

            val timeTaken = _solveTimerSeconds.value
            if (isSuccess) {
                stopTimer()
            }

            val submission = SubmissionEntity(
                problemId = problem.id,
                problemTitle = problem.title,
                problemDifficulty = problem.difficulty,
                code = code,
                language = lang,
                result = submissionResultString,
                errorMessage = result.stderr ?: result.compile_output,
                timestamp = System.currentTimeMillis(),
                timeTakenSeconds = timeTaken
            )

            repository.saveSubmission(submission)

            // Dynamic User Profile Streaks, Points and Badges calculation
            if (isSuccess) {
                updateGamificationOnSuccess(problem, timeTaken, lang)
            }
        }
    }

    private suspend fun updateGamificationOnSuccess(problem: ProblemEntity, timeTaken: Int, lang: String) {
        val profile = userProfile.value ?: UserProfileEntity(
            name = "Expert Developer",
            email = "developer@codequest.ai",
            overallStreak = 1,
            points = 0,
            unlockedBadges = ""
        )
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val currentStreak = if (profile.lastActiveDate == today) {
            profile.overallStreak
        } else {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            
            if (profile.lastActiveDate == yesterday) {
                profile.overallStreak + 1
            } else if (profile.lastActiveDate.isEmpty()) {
                1
            } else {
                1 // reset on broken streak
            }
        }

        // Base points based on difficulty
        val basePoints = when (problem.difficulty.lowercase()) {
            "easy" -> 100
            "medium" -> 200
            "hard" -> 300
            else -> 100
        }

        // Speed bonus
        val speedBonus = when {
            timeTaken in 1..29 -> 100
            timeTaken in 30..59 -> 50
            timeTaken in 60..119 -> 20
            else -> 0
        }

        val streakBonus = currentStreak * 10
        val totalEarned = basePoints + speedBonus + streakBonus

        // Achievement badge check
        val currentBadgesList = profile.unlockedBadges.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableSet()
            
        val newlyUnlocked = mutableListOf<String>()

        // 1. "first_solve"
        if (!currentBadgesList.contains("first_solve")) {
            currentBadgesList.add("first_solve")
            newlyUnlocked.add("first_solve")
        }

        // 2. "three_streak"
        if (currentStreak >= 3 && !currentBadgesList.contains("three_streak")) {
            currentBadgesList.add("three_streak")
            newlyUnlocked.add("three_streak")
        }

        // 3. "hard_master"
        if (problem.difficulty.lowercase() == "hard" && !currentBadgesList.contains("hard_master")) {
            currentBadgesList.add("hard_master")
            newlyUnlocked.add("hard_master")
        }

        // 5. "speed_demon"
        if (timeTaken in 1..29 && !currentBadgesList.contains("speed_demon")) {
            currentBadgesList.add("speed_demon")
            newlyUnlocked.add("speed_demon")
        }

        // Gather successful submissions from database to check high counts
        val allSubs = allSubmissions.value
        val successSubsList = allSubs.filter { it.result == "Accepted" }
        val uniqueSolvedProblemIds = (successSubsList.map { it.problemId } + problem.id).distinct()

        // 4. "ten_solves" (Algorithm master: solved 3 or more distinct problems)
        val solvedCount = uniqueSolvedProblemIds.size
        if (solvedCount >= 3 && !currentBadgesList.contains("ten_solves")) {
            currentBadgesList.add("ten_solves")
            newlyUnlocked.add("ten_solves")
        }

        // 6. "polyglot" (Languages >= 2)
        val distinctLangs = (successSubsList.map { it.language } + lang).distinct()
        if (distinctLangs.size >= 2 && !currentBadgesList.contains("polyglot")) {
            currentBadgesList.add("polyglot")
            newlyUnlocked.add("polyglot")
        }

        val badgesStr = currentBadgesList.joinToString(",")

        val updatedProfile = profile.copy(
            overallStreak = currentStreak,
            lastActiveDate = today,
            points = profile.points + totalEarned,
            unlockedBadges = badgesStr
        )
        repository.saveUserProfile(updatedProfile)

        _solveReward.value = SolveReward(
            basePoints = basePoints,
            speedBonus = speedBonus,
            streakBonus = streakBonus,
            totalEarned = totalEarned,
            newBadges = newlyUnlocked
        )
    }

    // --- AI Operations ---

    fun generateLinkedInPost() {
        val profile = userProfile.value ?: return
        val submissions = allSubmissions.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Filter submissions solved today
        val todaySucceeded = submissions.filter {
            val subDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            it.result == "Accepted" && subDate == todayStr
        }

        val solvedCountToday = todaySucceeded.distinctBy { it.problemId }.size
        val solvedNames = todaySucceeded.distinctBy { it.problemId }.map { it.problemTitle }

        _isGeneratingPost.value = true
        _linkedInPostText.value = ""

        viewModelScope.launch {
            val text = GeminiApiClient.generateLinkedInContent(
                userName = profile.name,
                solvedCountToday = solvedCountToday,
                solvedProblems = solvedNames,
                streak = profile.overallStreak
            )
            _linkedInPostText.value = text
            _isGeneratingPost.value = false
        }
    }

    fun loadAIInsights() {
        val submissions = allSubmissions.value
        if (submissions.isEmpty()) {
            _aiInsights.value = AIInsightsResult(
                weakTopics = listOf("Inconclusive Profile"),
                recommendation = "Solve at least one coding challenge in CodeQuest to unlock personalized AI reviews.",
                feedbackMessage = "Submit custom code snippets and run logic diagnostics to profile weaker topics."
            )
            return
        }

        // Limit the submission JSON payload to past 20 items to conserve token limits
        val selectedRecent = submissions.take(20).map {
            mapOf(
                "title" to it.problemTitle,
                "difficulty" to it.problemDifficulty,
                "language" to it.language,
                "result" to it.result,
                "error" to (it.errorMessage ?: "")
            )
        }

        val gsonString = selectedRecent.joinToString(prefix = "[", postfix = "]") { map ->
            "{\"title\":\"${map["title"]}\",\"difficulty\":\"${map["difficulty"]}\",\"language\":\"${map["language"]}\",\"result\":\"${map["result"]}\"}"
        }

        _isGeneratingInsights.value = true
        _aiInsights.value = null

        viewModelScope.launch {
            val result = GeminiApiClient.getPerformanceInsights(gsonString)
            _aiInsights.value = result
            _isGeneratingInsights.value = false
        }
    }
}
