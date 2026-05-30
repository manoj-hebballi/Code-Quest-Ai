package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.data.database.SubmissionEntity
import com.example.data.network.AIEvaluationResult
import com.example.ui.theme.*
import com.example.ui.viewmodel.CodePracticeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodingScreen(
    viewModel: CodePracticeViewModel,
    modifier: Modifier = Modifier
) {
    val problem by viewModel.selectedProblem.collectAsState()
    val language by viewModel.selectedLanguage.collectAsState()
    val enteredCode by viewModel.enteredCode.collectAsState()
    val isEvaluating by viewModel.isEvaluating.collectAsState()
    val evalResult by viewModel.evaluationResult.collectAsState()
    val trials by viewModel.currentProblemSubmissions.collectAsState()
    val solveReward by viewModel.solveReward.collectAsState()

    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Details, 1: Editor, 2: History

    val activeProblem = problem ?: return

    val diffColor = when (activeProblem.difficulty.lowercase()) {
        "easy" -> EasyGreen
        "medium" -> MediumGold
        "hard" -> HardCrimson
        else -> TextLightGrey
    }

    solveReward?.let { reward ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSolveReward() },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearSolveReward() },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeMintGreen),
                    modifier = Modifier.testTag("reward_dismiss_btn")
                ) {
                    Text("Awesome!", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CodeMintGreen.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Success celebration Trophies",
                            tint = CodeMintGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Challenge Solved!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextCrispWhite,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Excellent work! Your compiling code successfully resolved all problem test cases. Here are your gamification rewards:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightGrey,
                        textAlign = TextAlign.Center
                    )
                    
                    HorizontalDivider(color = LightSlateBorder.copy(alpha = 0.6f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Problem points:", style = MaterialTheme.typography.bodyMedium, color = TextLightGrey)
                        Text("+${reward.basePoints} PTS", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextCrispWhite)
                    }
                    
                    if (reward.speedBonus > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = "Speed bonus", tint = CodeMintGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Speed bonus dynamic:", style = MaterialTheme.typography.bodyMedium, color = TextLightGrey)
                            }
                            Text("+${reward.speedBonus} PTS", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CodeMintGreen)
                        }
                    }

                    if (reward.streakBonus > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ElectricBolt, contentDescription = "Streak bonus", tint = MediumGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Streak bonus multiplier:", style = MaterialTheme.typography.bodyMedium, color = TextLightGrey)
                            }
                            Text("+${reward.streakBonus} PTS", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MediumGold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MediumSlateCard)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Points Earned:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextCrispWhite)
                        Text("+${reward.totalEarned} PTS", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = CodeMintGreen)
                    }

                    if (reward.newBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🏵 NEW BADGES UNLOCKED!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MediumGold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        
                        reward.newBadges.forEach { badgeId ->
                            val badgeTitle = when (badgeId) {
                                "first_solve" -> "First Step"
                                "three_streak" -> "Streak Master"
                                "hard_master" -> "Hardcore Solved"
                                "ten_solves" -> "Algorithm Ninja"
                                "speed_demon" -> "Speed Demon"
                                "polyglot" -> "Polyglot"
                                else -> "New Achievement"
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFB45309).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFB45309).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Badge unlocked",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = badgeTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCrispWhite
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = DarkSlateSurface,
            titleContentColor = TextCrispWhite,
            textContentColor = TextLightGrey
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeProblem.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextCrispWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeProblem.difficulty,
                                style = MaterialTheme.typography.labelSmall,
                                color = diffColor,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                color = TextLightGrey,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeProblem.tags,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightGrey
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.selectProblem(null) },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextCrispWhite
                        )
                    }
                },
                actions = {
                    val timerVal by viewModel.solveTimerSeconds.collectAsState()
                    val minutes = timerVal / 60
                    val seconds = timerVal % 60
                    val speedTimeText = String.format("%02d:%02d", minutes, seconds)
                    
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MediumSlateCard)
                            .border(1.dp, LightSlateBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Active practice timer",
                                tint = CodeMintGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = speedTimeText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextCrispWhite
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateSurface)
            )
        },
        containerColor = ObsidianBlack,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- WORKSPACE MAIN TABS ---
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = DarkSlateSurface,
                contentColor = CodeMintGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = CodeMintGreen
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Details", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_details")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Workspace", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_editor")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Attempts (${trials.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            // --- TAB CONTENT DISPATCHER ---
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> ProblemDetailsTab(activeProblem)
                    1 -> EditorWorkspaceTab(
                        viewModel = viewModel,
                        language = language,
                        enteredCode = enteredCode,
                        isEvaluating = isEvaluating,
                        evalResult = evalResult
                    )
                    2 -> AttemptHistoryTab(trials = trials)
                }
            }
        }
    }
}

// =================== TAB 1: DETAILS ===================

@Composable
fun ProblemDetailsTab(problem: com.example.data.database.ProblemEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LightSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CodeMintGreen
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = problem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = TextCrispWhite
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = LightSlateBorder, thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Topic Scope",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TechElectricBlue
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    problem.tags.split(",").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MediumSlateCard)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag.trim(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightGrey,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// =================== TAB 2: EDITOR WORKSPACE ===================

@Composable
fun EditorWorkspaceTab(
    viewModel: CodePracticeViewModel,
    language: String,
    enteredCode: String,
    isEvaluating: Boolean,
    evalResult: AIEvaluationResult?
) {
    val languages = listOf("Kotlin", "Python", "Java")
    var isExpandedLang by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- TOOLBAR ENGINE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language selector button
            Box {
                Button(
                    onClick = { isExpandedLang = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, LightSlateBorder),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("language_selector")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(language, color = TextCrispWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown", tint = TextLightGrey, modifier = Modifier.size(16.dp))
                    }
                }
                DropdownMenu(
                    expanded = isExpandedLang,
                    onDismissRequest = { isExpandedLang = false },
                    modifier = Modifier.background(DarkSlateSurface).border(1.dp, LightSlateBorder)
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang, color = TextCrispWhite) },
                            onClick = {
                                viewModel.setLanguage(lang)
                                isExpandedLang = false
                            },
                        )
                    }
                }
            }

            // Restore starting template button
            IconButton(
                onClick = { viewModel.resetWorkspace() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSlateSurface)
                    .border(1.dp, LightSlateBorder, CircleShape)
                    .testTag("reset_code_button")
            ) {
                Icon(Icons.Default.SettingsBackupRestore, contentDescription = "Restore template", tint = TextLightGrey, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- MODERN CODE EDITOR WINDOW ---
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LightSlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Editor Line counters column
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(DarkSlateSurface)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val lineCount = enteredCode.lines().size.coerceAtLeast(1)
                    for (i in 1..lineCount) {
                        Text(
                            text = "$i",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextLightGrey.copy(alpha = 0.5f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }

                // Main Monospace Text Field
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianBlack)
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = enteredCode,
                        onValueChange = { viewModel.onCodeEdited(it) },
                        textStyle = TextStyle(
                            color = TextCrispWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(CodeMintGreen),
                        visualTransformation = CodeSyntaxTransformation(),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_textarea")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SUBMIT WORK BUTTON ---
        Button(
            onClick = { viewModel.submitCode() },
            colors = ButtonDefaults.buttonColors(containerColor = CodeMintGreen),
            shape = RoundedCornerShape(12.dp),
            enabled = !isEvaluating && enteredCode.trim().isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("code_submit_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEvaluating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ObsidianBlack, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compiling Logic...", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = ObsidianBlack)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Execute & Submit Code", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- COMPILER OUTPUT FEEDBACK BOARD ---
        AnimatedVisibility(
            visible = evalResult != null || isEvaluating,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            if (isEvaluating) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LightSlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = CodeMintGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Running Sandbox Testbeds", color = TextCrispWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Verifying corner loops against constraints in sandbox...", color = TextLightGrey, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else if (evalResult != null) {
                CompilerOutcomeSheet(evalResult)
            }
        }
    }
}

@Composable
fun CompilerOutcomeSheet(result: AIEvaluationResult) {
    val isAccepted = result.status_id == 3
    val accentColor = when (result.status_id) {
        3 -> EasyGreen
        4 -> MediumGold
        6 -> HardCrimson
        else -> HardCrimson
    }

    val outcomeHeadline = when (result.status_id) {
        3 -> "ACCEPTED"
        4 -> "WRONG ANSWER"
        6 -> "COMPILE ERROR"
        else -> "LOGIC OR RUNTIME ERROR"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().testTag("compiler_sheet")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAccepted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = "Status icon",
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = outcomeHeadline,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Status: ${result.status_id}",
                        color = accentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Console outputs if available
            if (!result.stdout.isNullOrEmpty() || !result.compile_output.isNullOrEmpty() || !result.stderr.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Standard Out / Debug Traces:", style = MaterialTheme.typography.bodySmall, color = TextLightGrey)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianBlack)
                        .padding(10.dp)
                ) {
                    val logsText = result.stdout ?: result.compile_output ?: result.stderr ?: ""
                    Text(
                        text = logsText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (isAccepted) CodeMintGreen else HardCrimson
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Explanation review blocks
            Divider(color = LightSlateBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.School,
                    contentDescription = "AI Coach feedback",
                    tint = CodeMintGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI Coding Coach Notes:",
                        style = MaterialTheme.typography.labelLarge,
                        color = CodeMintGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextCrispWhite,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// =================== TAB 3: ATTEMPT HISTORY ===================

@Composable
fun AttemptHistoryTab(trials: List<SubmissionEntity>) {
    if (trials.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    Icons.Default.HistoryEdu,
                    contentDescription = "Empty submissions history",
                    tint = TextLightGrey,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Submissions Logged",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCrispWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your submission timeline traces logic optimizations from brute-force to pristine solutions offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLightGrey,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("submissions_timeline")
        ) {
            items(trials) { submission ->
                var isExpandedCode by remember { mutableStateOf(false) }
                val sdf = remember { SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()) }
                val timeLabel = sdf.format(Date(submission.timestamp))

                val badgeColor = when (submission.result) {
                    "Accepted" -> EasyGreen
                    "Wrong Answer" -> MediumGold
                    "Compile Error" -> HardCrimson
                    else -> HardCrimson
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isExpandedCode) badgeColor.copy(alpha = 0.5f) else LightSlateBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpandedCode = !isExpandedCode }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = submission.result,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = badgeColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$timeLabel (${submission.language})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextLightGrey
                                )
                            }

                            Icon(
                                imageVector = if (isExpandedCode) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "toggle code reveal",
                                tint = TextLightGrey
                            )
                        }

                        // Code details dropdown box
                        AnimatedVisibility(visible = isExpandedCode) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text("Submitted Logic Draft:", style = MaterialTheme.typography.labelSmall, color = TextLightGrey)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ObsidianBlack)
                                        .horizontalScroll(rememberScrollState())
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = submission.code,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = TextCrispWhite
                                    )
                                }
                                
                                if (!submission.errorMessage.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Compiler Error Records:", style = MaterialTheme.typography.labelSmall, color = HardCrimson)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = submission.errorMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HardCrimson,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =================== CUSTOM CODE COLORING LOGICS ===================

class CodeSyntaxTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val keywords = listOf(
            "fun", "class", "def", "val", "var", "return", "import", "public", "private", "class",
            "int", "void", "char", "boolean", "int[]", "Solution", "height", "target", "nums"
        )
        val highlighted = AnnotatedString.Builder().apply {
            val textString = text.text
            append(textString)
            
            // Loop and highlight keywords
            keywords.forEach { keyword ->
                var index = textString.indexOf(keyword)
                while (index >= 0) {
                    addStyle(
                        style = SpanStyle(
                            color = if (keyword == "fun" || keyword == "class" || keyword == "def" || keyword == "return") CodePurple else CodeYellow,
                            fontWeight = FontWeight.Bold
                        ),
                        start = index,
                        end = index + keyword.length
                    )
                    index = textString.indexOf(keyword, index + 1)
                }
            }
        }.toAnnotatedString()

        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}
