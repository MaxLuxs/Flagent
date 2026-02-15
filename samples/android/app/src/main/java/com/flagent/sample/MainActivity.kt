package com.flagent.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flagent.sample.ui.screens.BatchEvaluationScreen
import com.flagent.sample.ui.screens.DebugScreen
import com.flagent.sample.ui.screens.EvaluationScreen
import com.flagent.sample.ui.screens.HomeScreen
import com.flagent.sample.ui.screens.SettingsScreen
import com.flagent.sample.ui.theme.FlagentTheme
import com.flagent.sample.viewmodel.SettingsViewModel

sealed class Screen(val title: String, val icon: ImageVector) {
    data object Home : Screen("Home", Icons.Default.Home)
    data object Evaluation : Screen("Evaluation", Icons.Default.List)
    data object BatchEvaluation : Screen("Batch", Icons.Default.List)
    data object Debug : Screen("Debug", Icons.Default.BugReport)
    data object Settings : Screen("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlagentTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val settingsViewModel: SettingsViewModel = viewModel()
    val settings by settingsViewModel.settings.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Screen.Home.icon, contentDescription = Screen.Home.title) },
                    label = { Text(Screen.Home.title) },
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.Evaluation.icon, contentDescription = Screen.Evaluation.title) },
                    label = { Text("Eval") },
                    selected = currentScreen == Screen.Evaluation,
                    onClick = { currentScreen = Screen.Evaluation }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.BatchEvaluation.icon, contentDescription = Screen.BatchEvaluation.title) },
                    label = { Text("Batch") },
                    selected = currentScreen == Screen.BatchEvaluation,
                    onClick = { currentScreen = Screen.BatchEvaluation }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.Debug.icon, contentDescription = Screen.Debug.title) },
                    label = { Text(Screen.Debug.title) },
                    selected = currentScreen == Screen.Debug,
                    onClick = { currentScreen = Screen.Debug }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.Settings.icon, contentDescription = Screen.Settings.title) },
                    label = { Text(Screen.Settings.title) },
                    selected = currentScreen == Screen.Settings,
                    onClick = { currentScreen = Screen.Settings }
                )
            }
        }
    ) { paddingValues ->
        when (currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    onNavigateToEvaluation = { currentScreen = Screen.Evaluation },
                    onNavigateToBatchEvaluation = { currentScreen = Screen.BatchEvaluation },
                    onNavigateToSettings = { currentScreen = Screen.Settings },
                    onNavigateToDebug = { currentScreen = Screen.Debug },
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is Screen.Evaluation -> {
                EvaluationScreen(
                    baseUrl = settings.baseUrl
                )
            }
            is Screen.BatchEvaluation -> {
                BatchEvaluationScreen(
                    baseUrl = settings.baseUrl
                )
            }
            is Screen.Debug -> {
                DebugScreen(
                    settingsViewModel = settingsViewModel
                )
            }
            is Screen.Settings -> {
                SettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
