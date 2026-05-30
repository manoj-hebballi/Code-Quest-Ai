package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SubmissionEntity
import com.example.data.database.UserProfileEntity
import com.example.data.network.AIInsightsResult
import com.example.ui.theme.*
import com.example.ui.viewmodel.CodePracticeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: CodePracticeViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val submissions by viewModel.allSubmissions.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val isGeneratingInsights by viewModel.isGeneratingInsights.collectAsState()
    val context = LocalContext.current

    // Trigger AI Insights on first launch if empty
    LaunchedEffect(submissions) {
        if (aiInsights == null && submissions.isNotEmpty() && !isGeneratingInsights) {
            viewModel.loadAIInsights()
        }
    }

    Scaffold(
        containerColor = ObsidianBlack,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- HEADER PROFILE CARD ---
            UserProfileHeader(profile = profile)

            Spacer(modifier = Modifier.height(20.dp))

            // --- GITHUB HEATMAP SECTION ---
            Text(
                text = "Consistency Heatmap",
                style = MaterialTheme.typography.titleMedium,
                color = TextCrispWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Daily activity trackers across the last 12 weeks of practice",
                style = MaterialTheme.typography.bodySmall,
                color = TextLightGrey
            )
            Spacer(modifier = Modifier.height(8.dp))
            ContributionHeatmap(submissions = submissions)

            Spacer(modifier = Modifier.height(24.dp))

            // --- STATS DONUT DECK ---
            Text(
                text = "Performance Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = TextCrispWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            DifficultyDonutCard(submissions = submissions)

            Spacer(modifier = Modifier.height(24.dp))

            // --- GAMIFICATION ARENA HUB ---
            GamificationArenaHub(profile = profile)

            Spacer(modifier = Modifier.height(24.dp))

            // --- AI INSIGHT COMPONENT ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Smart Coaching Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCrispWhite,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.loadAIInsights() },
                    enabled = !isGeneratingInsights,
                    modifier = Modifier.testTag("ai_insights_refresh")
                ) {
                    if (isGeneratingInsights) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = CodeMintGreen)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Insights", tint = CodeMintGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            AIInsightsCard(
                isLoading = isGeneratingInsights,
                insights = aiInsights
            )
        }
    }
}

@Composable
fun UserProfileHeader(profile: UserProfileEntity?) {
    val name = profile?.name ?: "Expert Coder"
    val streak = profile?.overallStreak ?: 0

    // Elegant gradient representation for the avatar disk in clean minimalism HTML
    val avatarGradient = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(Color(0xFFD0BCFF), Color(0xFF6750A4))
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, LightSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI CODING HUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = CodeMintGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextCrispWhite,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EasyGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Sandbox compiled",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightGrey,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Premium Minimalist Avatar representation or streak badges
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Points pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MediumSlateCard)
                            .border(1.dp, LightSlateBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${profile?.points ?: 1250}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = CodeMintGreen
                            )
                            Text(
                                text = "POINTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLightGrey
                            )
                        }
                    }

                    // Consistent streak pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MediumSlateCard)
                            .border(1.dp, LightSlateBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$streak",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = TextCrispWhite
                            )
                            Text(
                                text = "DAYS",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLightGrey
                            )
                        }
                    }

                    // Rounded avatar border with gradient filling from HTML spec
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(avatarGradient)
                            .border(1.5.dp, LightSlateBorder, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ContributionHeatmap(submissions: List<SubmissionEntity>) {
    val context = LocalContext.current
    var focusedCellInfo by remember { mutableStateOf<String?>(null) }

    // Map submissions parsed today
    val acceptedCountByDate = remember(submissions) {
        submissions
            .filter { it.result == "Accepted" }
            .groupBy {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            }
            .mapValues { it.value.size }
    }

    // Trailing 12 weeks of grids
    val calendarGrid = remember {
        val grid = mutableListOf<List<Date>>()
        val cal = Calendar.getInstance()
        
        // Snap to start of active column week (Sunday)
        cal.add(Calendar.WEEK_OF_YEAR, -11)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        
        for (w in 0..11) {
            val weekDays = mutableListOf<Date>()
            for (d in 0..6) {
                weekDays.add(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            grid.add(weekDays)
        }
        grid
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                calendarGrid.forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { date ->
                            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                            val solveCount = acceptedCountByDate[dateString] ?: 0
                            
                            val cellColor = when {
                                solveCount == 0 -> MediumSlateCard
                                solveCount <= 1 -> Color(0xFFEADDFF)
                                solveCount <= 2 -> Color(0xFFD0BCFF)
                                else -> CodeMintGreen
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                                    .clickable {
                                        val readableDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
                                        focusedCellInfo = if (solveCount == 1) {
                                            "On $readableDate: Solved 1 coding algorithm!"
                                        } else {
                                            "On $readableDate: Solved $solveCount algorithms!"
                                        }
                                        Toast.makeText(context, focusedCellInfo, Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cell details feedback display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MediumSlateCard)
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalActivity,
                        contentDescription = "Hint info",
                        tint = CodeMintGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = focusedCellInfo ?: "Tap grid cells to inspect specific execution logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (focusedCellInfo != null) TextCrispWhite else TextLightGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key guidelines
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall, color = TextLightGrey)
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(10.dp).background(MediumSlateCard, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFEADDFF), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFD0BCFF), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(10.dp).background(CodeMintGreen, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("More", style = MaterialTheme.typography.labelSmall, color = TextLightGrey)
            }
        }
    }
}

@Composable
fun DifficultyDonutCard(submissions: List<SubmissionEntity>) {
    val accepted = remember(submissions) { submissions.filter { it.result == "Accepted" } }
    
    val total = accepted.size
    val easyCount = accepted.count { it.problemDifficulty.equals("Easy", ignoreCase = true) }
    val mediumCount = accepted.count { it.problemDifficulty.equals("Medium", ignoreCase = true) }
    val hardCount = accepted.count { it.problemDifficulty.equals("Hard", ignoreCase = true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Native Canvas Donut Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val canvasSize = size
                    val ringBounds = size.width - strokeWidth
                    val sizeToDraw = Size(ringBounds, ringBounds)

                    if (total == 0) {
                        // Draw empty placeholder ring
                        drawArc(
                            color = LightSlateBorder,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
                            size = sizeToDraw,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    } else {
                        val easySweep = (easyCount.toFloat() / total) * 360f
                        val mediumSweep = (mediumCount.toFloat() / total) * 360f
                        val hardSweep = (hardCount.toFloat() / total) * 360f

                        var startAngle = -90f

                        // Draw Easy Green
                        if (easySweep > 0f) {
                            drawArc(
                                color = EasyGreen,
                                startAngle = startAngle,
                                sweepAngle = easySweep,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
                                size = sizeToDraw,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += easySweep
                        }

                        // Draw Medium Amber Gold
                        if (mediumSweep > 0f) {
                            drawArc(
                                color = MediumGold,
                                startAngle = startAngle,
                                sweepAngle = mediumSweep,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
                                size = sizeToDraw,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += mediumSweep
                        }

                        // Draw Hard Crimson
                        if (hardSweep > 0f) {
                            drawArc(
                                color = HardCrimson,
                                startAngle = startAngle,
                                sweepAngle = hardSweep,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
                                size = sizeToDraw,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$total",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextCrispWhite
                    )
                    Text(
                        text = "SOLVED",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = TextLightGrey
                    )
                }
            }

            // Stat Legends List
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 12.dp)
            ) {
                StatLegendItem(color = EasyGreen, label = "Easy", count = easyCount)
                StatLegendItem(color = MediumGold, label = "Medium", count = mediumCount)
                StatLegendItem(color = HardCrimson, label = "Hard", count = hardCount)
            }
        }
    }
}

@Composable
fun StatLegendItem(color: Color, label: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.width(130.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLightGrey
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextCrispWhite
        )
    }
}

@Composable
fun AIInsightsCard(
    isLoading: Boolean,
    insights: AIInsightsResult?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LightSlateBorder),
        modifier = Modifier.fillMaxWidth().testTag("ai_insights_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = CodeMintGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Gemini is profiling your recent error traces...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightGrey,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (insights != null) {
                // Topic Chips headers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = "Study guides",
                        tint = CodeMintGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Focus Target Topics:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextCrispWhite
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    insights.weakTopics.forEach { topic ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MediumGold.copy(alpha = 0.15f))
                                .border(1.dp, MediumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.labelSmall,
                                color = MediumGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed message
                Text(
                    text = "Weekly Coach Analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLightGrey,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insights.feedbackMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCrispWhite
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actionable recommendation
                Card(
                    colors = CardDefaults.cardColors(containerColor = MediumSlateCard),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = "Growth tip",
                            tint = CodeMintGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Action Plan",
                                style = MaterialTheme.typography.labelMedium,
                                color = CodeMintGreen,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = insights.recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextCrispWhite
                            )
                        }
                    }
                }

            } else {
                // Empty state or prompt to practice
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.HistoryEdu,
                        contentDescription = "Practice icon",
                        tint = TextLightGrey,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Analytics Ready",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextCrispWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Solve code exercises and submit algorithms to generate custom coach reports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// =================== EXTRA COMPOSABLES: GAMIFICATION ARENA HUB ===================

@Composable
fun GamificationArenaHub(profile: com.example.data.database.UserProfileEntity?) {
    var activeArenaTab by remember { mutableIntStateOf(0) } // 0: Leaderboard, 1: Badges

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, LightSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section title and total points indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gamification Arena",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextCrispWhite
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MediumSlateCard)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Your Points: ${profile?.points ?: 1250} Pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = CodeMintGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub tab rows
            TabRow(
                selectedTabIndex = activeArenaTab,
                containerColor = MediumSlateCard,
                contentColor = CodeMintGreen,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeArenaTab == 0,
                    onClick = { activeArenaTab = 0 },
                    text = { Text("Leaderboard", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_arena_leaderboard")
                )
                Tab(
                    selected = activeArenaTab == 1,
                    onClick = { activeArenaTab = 1 },
                    text = { Text("Achievements & Badges", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_arena_badges")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeArenaTab) {
                0 -> LeaderboardSubTab(profile)
                1 -> BadgesSubTab(profile)
            }
        }
    }
}

data class LeaderboardPeer(
    val name: String,
    val points: Int,
    val streak: Int,
    val isCurrentUser: Boolean = false,
    val avatarGradient: androidx.compose.ui.graphics.Brush
)

@Composable
fun LeaderboardSubTab(profile: com.example.data.database.UserProfileEntity?) {
    val leaderboardPeers = remember(profile) {
        val userPoints = profile?.points ?: 1250
        val userStreak = profile?.overallStreak ?: 5
        val userName = profile?.name ?: "You"

        val staticPeers = listOf(
            LeaderboardPeer("CodeWizard_99", 4800, 15, false, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)))),
            LeaderboardPeer("ByteQueen", 3420, 12, false, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF7C3AED)))),
            LeaderboardPeer("AlgoSpecialist", 2200, 9, false, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF3B82F6)))),
            LeaderboardPeer("StackCompiler", 1150, 6, false, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF87171), Color(0xFFEF4444)))),
            LeaderboardPeer("RecursionRebel", 980, 4, false, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF10B981)))),
            LeaderboardPeer("KotlinScripter", 650, 3, false, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFF475569))))
        )

        val merged = staticPeers + LeaderboardPeer(
            name = userName,
            points = userPoints,
            streak = userStreak,
            isCurrentUser = true,
            avatarGradient = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFD0BCFF), Color(0xFF6750A4)))
        )

        merged.sortedByDescending { it.points }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Competition Rankings",
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGrey,
            fontWeight = FontWeight.Bold
        )

        leaderboardPeers.forEachIndexed { index, peer ->
            val rankNum = index + 1
            val backgroundColor = if (peer.isCurrentUser) CodeMintGreen.copy(alpha = 0.15f) else Color.Transparent
            val borderModifier = if (peer.isCurrentUser) {
                Modifier.border(1.5.dp, CodeMintGreen, RoundedCornerShape(12.dp))
            } else {
                Modifier.border(1.dp, LightSlateBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .then(borderModifier)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Rank Indicator
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when (rankNum) {
                                        1 -> Color(0xFFFFD700)
                                        2 -> Color(0xFFC0C0C0)
                                        3 -> Color(0xFFCD7F32)
                                        else -> MediumSlateCard
                                    }
                                )
                        ) {
                            Text(
                                text = "$rankNum",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (rankNum <= 3) ObsidianBlack else TextCrispWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Peer Avatar
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(peer.avatarGradient)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (peer.isCurrentUser) "${peer.name} (You)" else peer.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (peer.isCurrentUser) FontWeight.Black else FontWeight.SemiBold,
                                color = TextCrispWhite
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "Active coder streak",
                                    tint = MediumGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${peer.streak} Days active streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextLightGrey
                                )
                            }
                        }
                    }

                    // Score indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MediumSlateCard)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${peer.points} PTS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = if (peer.isCurrentUser) CodeMintGreen else TextCrispWhite
                        )
                    }
                }
            }
        }
    }
}

data class BadgeInfo(
    val id: String,
    val title: String,
    val description: String,
    val systemIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@Composable
fun BadgesSubTab(profile: com.example.data.database.UserProfileEntity?) {
    val unlockedSet = remember(profile) {
        profile?.unlockedBadges?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: setOf("first_solve")
    }

    val badgesList = listOf(
        BadgeInfo("first_solve", "First Step", "Earned after compiling your first successful challenge solve.", Icons.Default.EmojiEvents, Color(0xFF6750A4)),
        BadgeInfo("three_streak", "Streak Master", "Maintained an active algorithm solving streak of 3+ days.", Icons.Default.ElectricBolt, Color(0xFFB45309)),
        BadgeInfo("hard_master", "Hardcore Solved", "Mastered at least one Hard challenge constraints.", Icons.Default.Psychology, Color(0xFFB91C1C)),
        BadgeInfo("ten_solves", "Algorithm Ninja", "Mastered 3 or more unique algorithmic tasks.", Icons.Default.Shield, Color(0xFF148752)),
        BadgeInfo("speed_demon", "Speed Demon", "Created a successful compiling solution in under 30s.", Icons.Default.Timer, Color(0xFF7C3AED)),
        BadgeInfo("polyglot", "Polyglot", "Created valid compiling solutions across 2+ distinct programming languages.", Icons.Default.Translate, Color(0xFF625B71))
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Your Trophy Cabinet (${unlockedSet.size}/${badgesList.size})",
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGrey,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeCard(badgesList[0], unlockedSet.contains(badgesList[0].id), modifier = Modifier.weight(1f))
                BadgeCard(badgesList[1], unlockedSet.contains(badgesList[1].id), modifier = Modifier.weight(1f))
            }
            // Row 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeCard(badgesList[2], unlockedSet.contains(badgesList[2].id), modifier = Modifier.weight(1f))
                BadgeCard(badgesList[3], unlockedSet.contains(badgesList[3].id), modifier = Modifier.weight(1f))
            }
            // Row 3
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeCard(badgesList[4], unlockedSet.contains(badgesList[4].id), modifier = Modifier.weight(1f))
                BadgeCard(badgesList[5], unlockedSet.contains(badgesList[5].id), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun BadgeCard(badge: BadgeInfo, isUnlocked: Boolean, modifier: Modifier = Modifier) {
    val cardBg = if (isUnlocked) badge.accentColor.copy(alpha = 0.08f) else MediumSlateCard.copy(alpha = 0.5f)
    val borderCol = if (isUnlocked) badge.accentColor.copy(alpha = 0.4f) else LightSlateBorder.copy(alpha = 0.15f)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderCol),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Icon Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) badge.accentColor.copy(alpha = 0.15f) else MediumSlateCard)
            ) {
                Icon(
                    imageVector = if (isUnlocked) badge.systemIcon else Icons.Default.Lock,
                    contentDescription = badge.title,
                    tint = if (isUnlocked) badge.accentColor else TextLightGrey.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) TextCrispWhite else TextLightGrey,
                    maxLines = 1
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = TextLightGrey,
                    lineHeight = 11.sp,
                    maxLines = 2
                )
            }
        }
    }
}
