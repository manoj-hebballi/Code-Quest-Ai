package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ProblemEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CodePracticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemsScreen(
    viewModel: CodePracticeViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProblems by viewModel.filteredProblems.collectAsState()
    val allProblems by viewModel.allProblems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeDifficulty by viewModel.selectedDifficulty.collectAsState()
    val allSubmissions by viewModel.allSubmissions.collectAsState()

    // Find solved problem IDs
    val solvedIds = remember(allSubmissions) {
        allSubmissions
            .filter { it.result == "Accepted" }
            .map { it.problemId }
            .toSet()
    }

    Scaffold(
        containerColor = ObsidianBlack,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- PROGRESS TRACKER PROFILE DECK ---
            if (allProblems.isNotEmpty()) {
                val totalCount = allProblems.size
                val solvedCount = allProblems.count { solvedIds.contains(it.id) }
                val progressFraction = solvedCount.toFloat() / totalCount

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LightSlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = "Code Tracker",
                                    tint = CodeMintGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Your Practice Progress",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextCrispWhite
                                )
                            }
                            Text(
                                text = "$solvedCount / $totalCount Solved",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = CodeMintGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = CodeMintGreen,
                            trackColor = MediumSlateCard
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SEARCH BAR SECTION ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search topics, e.g. Arrays, Stacks...", color = TextLightGrey) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = TextLightGrey) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextCrispWhite,
                    unfocusedTextColor = TextCrispWhite,
                    focusedContainerColor = DarkSlateSurface,
                    unfocusedContainerColor = DarkSlateSurface,
                    focusedBorderColor = CodeMintGreen,
                    unfocusedBorderColor = LightSlateBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("problem_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- HORIZONTAL DIFFICULTIES CHIPS ---
            val difficulties = listOf("All", "Easy", "Medium", "Hard")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                difficulties.forEach { diff ->
                    val isSelected = activeDifficulty == diff
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CodeMintGreen else DarkSlateSurface)
                            .border(
                                1.dp,
                                if (isSelected) CodeMintGreen else LightSlateBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setDifficultyFilter(diff) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_chip_$diff")
                    ) {
                        Text(
                            text = diff,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ObsidianBlack else TextCrispWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PROBLEMS LIST ---
            if (filteredProblems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No problems match your filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLightGrey
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("problems_list")
                ) {
                    items(filteredProblems, key = { it.id }) { problem ->
                        val isSolved = solvedIds.contains(problem.id)
                        ProblemRowItem(
                            problem = problem,
                            isSolved = isSolved,
                            onClick = { viewModel.selectProblem(problem) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProblemRowItem(
    problem: ProblemEntity,
    isSolved: Boolean,
    onClick: () -> Unit
) {
    val diffColor = when (problem.difficulty.lowercase()) {
        "easy" -> EasyGreen
        "medium" -> MediumGold
        "hard" -> HardCrimson
        else -> TextLightGrey
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSolved) CodeMintGreen.copy(alpha = 0.35f) else LightSlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("problem_item_${problem.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Left Difficulty Bar Chip
                    Box(
                        modifier = Modifier
                            .size(height = 14.dp, width = 4.dp)
                            .clip(CircleShape)
                            .background(diffColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = problem.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCrispWhite,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Difficulty Tag
                    Text(
                        text = problem.difficulty,
                        style = MaterialTheme.typography.labelSmall,
                        color = diffColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text("•", color = LightSlateBorder, style = MaterialTheme.typography.labelSmall)

                    // Topic tags
                    val listTags = problem.tags.split(",")
                    listTags.take(2).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MediumSlateCard)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag.trim(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = TextLightGrey
                            )
                        }
                    }
                }
            }

            // Completion checkmark
            IconButton(onClick = onClick) {
                if (isSolved) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Solved algorithm",
                        tint = CodeMintGreen,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(1.5.dp, LightSlateBorder, CircleShape)
                    )
                }
            }
        }
    }
}
