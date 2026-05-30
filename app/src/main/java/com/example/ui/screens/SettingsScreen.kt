package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDatabase
import com.example.ui.theme.*
import com.example.ui.viewmodel.CodePracticeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: CodePracticeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.userProfile.collectAsState()

    // Form states
    var tempName by remember(profile) { mutableStateOf(profile?.name ?: "Expert Developer") }
    var tempEmail by remember(profile) { mutableStateOf(profile?.email ?: "developer@codequest.ai") }

    val host by viewModel.judge0Host.collectAsState()
    val apiKey by viewModel.judge0Key.collectAsState()

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

            // --- INFO TIP: AI FALLBACK NODE ACCENTS ---
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CodeMintGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Fallback Tip",
                        tint = CodeMintGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Zero-Config Compilation Node",
                            style = MaterialTheme.typography.labelLarge,
                            color = CodeMintGreen,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CodeQuest maps server-side Gemini logic validation natively. No compiler keys are needed! Leaving the compiler setup empty below triggers the AI sandbox to evaluate syntax errors, variables, and logic gates automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextCrispWhite,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 1: PROFILE MANAGERS ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = "Profile icon", tint = TechElectricBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Developer Account Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextCrispWhite
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tempName,
                onValueChange = { tempName = it },
                label = { Text("Developer Full Name", color = TextLightGrey) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
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
                    .testTag("settings_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = tempEmail,
                onValueChange = { tempEmail = it },
                label = { Text("Branding Email Address", color = TextLightGrey) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextCrispWhite,
                    unfocusedTextColor = TextCrispWhite,
                    focusedContainerColor = DarkSlateSurface,
                    unfocusedContainerColor = DarkSlateSurface,
                    focusedBorderColor = CodeMintGreen,
                    unfocusedBorderColor = LightSlateBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save details
            Button(
                onClick = {
                    viewModel.updateProfile(tempName, tempEmail)
                    Toast.makeText(context, "Account profile updated!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MediumSlateCard),
                border = BorderStroke(1.dp, LightSlateBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("settings_save_profile_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save profile", tint = TextCrispWhite, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile", color = TextCrispWhite)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- SECTION 2: COMPILER CREDENTIALS ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, contentDescription = "Compiler config", tint = TechElectricBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Remote Judge0 Compiling Setup (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextCrispWhite
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = host,
                onValueChange = { viewModel.judge0Host.value = it },
                placeholder = { Text("https://judge0-ce.p.rapidapi.com", color = TextLightGrey) },
                label = { Text("Judge0 Base URL Host", color = TextLightGrey) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
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
                    .testTag("settings_judge0_host")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { viewModel.judge0Key.value = it },
                placeholder = { Text("Enter RapidAPI Credentials Key", color = TextLightGrey) },
                label = { Text("X-RapidAPI-Key Token", color = TextLightGrey) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
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
                    .testTag("settings_judge0_key")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // --- SECTION 3: SYSTEM HYGIENE RESET ---
            Divider(color = LightSlateBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Reset limits", tint = HardCrimson, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Platform Maintenance Reset",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextCrispWhite
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Irreversibly delete current local submission history log sheets, resetting your contribution metrics, streaks, and profiling cache.",
                style = MaterialTheme.typography.bodySmall,
                color = TextLightGrey,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(context)
                        db.historyDao().clearAllSubmissions()
                        
                        // Seed back initial user state
                        db.historyDao().insertUserProfile(
                            com.example.data.database.UserProfileEntity(
                                id = "default_user",
                                name = "Expert Developer",
                                email = "developer@codequest.ai",
                                overallStreak = 0,
                                lastActiveDate = ""
                            )
                        )
                    }
                    Toast.makeText(context, "Practice attempts cleared successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HardCrimson.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, HardCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("settings_reset_database")
            ) {
                Text("Wipe Local Practice Records", color = HardCrimson, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
