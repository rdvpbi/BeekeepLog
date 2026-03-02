package com.beekeeplog.app.ui.navigation

/** Top-level navigation destinations. */
sealed class Screen(val route: String) {
    object Voice : Screen("voice")
    object Analytics : Screen("analytics")
}
