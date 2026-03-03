package com.beekeeplog.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.beekeeplog.app.presentation.voice.VoiceViewModel
import com.beekeeplog.app.ui.analytics.AnalyticsScreen
import com.beekeeplog.app.ui.voice.VoiceScreen

/** Root navigation graph with bottom navigation between Voice and Analytics screens. */
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val voiceViewModel: VoiceViewModel = hiltViewModel()
    val voiceUiState by voiceViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                voicePhase = voiceUiState.phase
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Voice.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Voice.route) {
                VoiceScreen(viewModel = voiceViewModel)
            }
            composable(Screen.Analytics.route) { AnalyticsScreen() }
        }
    }
}
