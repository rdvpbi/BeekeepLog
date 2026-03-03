package com.beekeeplog.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.beekeeplog.app.domain.model.VoicePhase

/** Bottom navigation bar. Analytics tab is blocked when voice session is active. */
@Composable
fun BottomNavBar(
    navController: NavController,
    voicePhase: VoicePhase = VoicePhase.IDLE
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val sessionActive = voicePhase != VoicePhase.IDLE

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Voice.route,
            onClick = {
                navController.navigate(Screen.Voice.route) {
                    launchSingleTop = true
                    popUpTo(Screen.Voice.route)
                }
            },
            icon = {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Запись",
                    modifier = androidx.compose.ui.Modifier.semantics {
                        contentDescription = "Вкладка запись"
                    }
                )
            },
            label = { Text("Запись") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Analytics.route,
            enabled = !sessionActive,
            onClick = {
                if (!sessionActive) {
                    navController.navigate(Screen.Analytics.route) {
                        launchSingleTop = true
                        popUpTo(Screen.Voice.route)
                    }
                }
            },
            icon = {
                Icon(
                    Icons.Filled.Analytics,
                    contentDescription = "Аналитика",
                    modifier = androidx.compose.ui.Modifier.semantics {
                        contentDescription = "Вкладка аналитика"
                    }
                )
            },
            label = { Text("Аналитика") }
        )
    }
}
