package com.beekeeplog.app.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/** Bottom navigation bar with Voice and Analytics tabs. */
@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Voice.route,
            onClick = {
                navController.navigate(Screen.Voice.route) {
                    launchSingleTop = true
                    popUpTo(Screen.Voice.route)
                }
            },
            icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
            label = { Text("Запись") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Analytics.route,
            onClick = {
                navController.navigate(Screen.Analytics.route) {
                    launchSingleTop = true
                    popUpTo(Screen.Voice.route)
                }
            },
            icon = { Icon(Icons.Filled.Analytics, contentDescription = null) },
            label = { Text("Аналитика") }
        )
    }
}
