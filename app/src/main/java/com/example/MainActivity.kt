package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CodePracticeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainControlAppScreen()
            }
        }
    }
}

@Composable
fun MainControlAppScreen() {
    val viewModel: CodePracticeViewModel = viewModel()
    val selectedProblem by viewModel.selectedProblem.collectAsState()

    var activeTab by remember { mutableIntStateOf(1) } // Default to Problems tab for rapid utility!

    // If a coding task is active, launch full bleed editor immediately!
    if (selectedProblem != null) {
        CodingScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSlateSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("app_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianBlack,
                            selectedTextColor = CodeMintGreen,
                            unselectedIconColor = TextLightGrey,
                            unselectedTextColor = TextLightGrey,
                            indicatorColor = CodeMintGreen
                        ),
                        modifier = Modifier.testTag("nav_btn_dashboard")
                    )

                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("Problems", fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 1) Icons.Filled.Code else Icons.Outlined.Code,
                                contentDescription = "Problems",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianBlack,
                            selectedTextColor = CodeMintGreen,
                            unselectedIconColor = TextLightGrey,
                            unselectedTextColor = TextLightGrey,
                            indicatorColor = CodeMintGreen
                        ),
                        modifier = Modifier.testTag("nav_btn_problems")
                    )

                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text("LinkedIn", fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 2) Icons.Filled.Share else Icons.Outlined.Share,
                                contentDescription = "LinkedIn Share",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianBlack,
                            selectedTextColor = CodeMintGreen,
                            unselectedIconColor = TextLightGrey,
                            unselectedTextColor = TextLightGrey,
                            indicatorColor = CodeMintGreen
                        ),
                        modifier = Modifier.testTag("nav_btn_linkedin")
                    )

                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        label = { Text("Settings", fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianBlack,
                            selectedTextColor = CodeMintGreen,
                            unselectedIconColor = TextLightGrey,
                            unselectedTextColor = TextLightGrey,
                            indicatorColor = CodeMintGreen
                        ),
                        modifier = Modifier.testTag("nav_btn_settings")
                    )
                }
            },
            containerColor = ObsidianBlack,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "tab_scrolling"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> DashboardScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                        1 -> ProblemsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                        2 -> LinkedInScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                        3 -> SettingsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
