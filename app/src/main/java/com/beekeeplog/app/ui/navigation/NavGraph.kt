package com.beekeeplog.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.beekeeplog.app.ui.analytics.AnalyticsScreen
import com.beekeeplog.app.ui.voice.VoiceScreen

/** Root navigation graph with bottom navigation between Voice and Analytics screens. */
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Voice.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Voice.route) { VoiceScreen() }
            composable(Screen.Analytics.route) { AnalyticsScreen() }
        }
    }
}
