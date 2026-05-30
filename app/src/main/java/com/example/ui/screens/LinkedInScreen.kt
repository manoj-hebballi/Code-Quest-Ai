package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CodePracticeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LinkedInScreen(
    viewModel: CodePracticeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isGenerating by viewModel.isGeneratingPost.collectAsState()
    val postText by viewModel.linkedInPostText.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val submissions by viewModel.allSubmissions.collectAsState()

    val todayStr = remember(submissions) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val todaySolvedList = remember(submissions, todayStr) {
        submissions.filter {
            val subDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            it.result == "Accepted" && subDate == todayStr
        }.distinctBy { it.problemId }
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
            Spacer(modifier = Modifier.height(8.dp))

            // --- TODAY'S ACHIEVEMENT CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LightSlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Streak Spark",
                        tint = MediumGold,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build Your Professional Brand!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextCrispWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Consistent career branding gets engineers noticed. Share today's completed algorithms on LinkedIn automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightGrey,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AchievementStat(
                            label = "Solved Today",
                            value = "${todaySolvedList.size} Tasks"
                        )
                        AchievementStat(
                            label = "Current Streak",
                            value = "${profile?.overallStreak ?: 0} Days"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- GENERATE POST BOARD ---
            Text(
                text = "Progress Post Editor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextCrispWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (isGenerating) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LightSlateBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = CodeMintGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Gemini is writing a high-impact branding post...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightGrey
                        )
                    }
                }
            } else if (postText.isNotEmpty()) {
                // Generated text block
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LightSlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 240.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianBlack)
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            Text(
                                text = postText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextCrispWhite,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Copy button
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("LinkedIn CodeQuest Post", postText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Post copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MediumSlateCard),
                                border = BorderStroke(1.dp, LightSlateBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("copy_post_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextCrispWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Text", color = TextCrispWhite, fontSize = 13.sp)
                            }

                            // Share Intent button
                            Button(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, postText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Post to Professional Socials")
                                    context.startActivity(shareIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CodeMintGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("share_post_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = ObsidianBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Launch Share", color = ObsidianBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Not generated empty state
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LightSlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = "Draft icon",
                            tint = TextLightGrey,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Draft Social Update",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextCrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create customized professional narratives highlighting your solved exercises.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightGrey,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.generateLinkedInPost() },
                            colors = ButtonDefaults.buttonColors(containerColor = CodeMintGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("generate_post_trigger")
                        ) {
                            Text("Draft My Post With AI", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = CodeMintGreen
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextLightGrey
        )
    }
}
