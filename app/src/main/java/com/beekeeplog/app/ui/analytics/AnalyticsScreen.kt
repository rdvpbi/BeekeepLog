package com.beekeeplog.app.ui.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.beekeeplog.app.ui.theme.NeonYellow

/** Analytics screen — KPI cards, filter chips and hive list. */
@Composable
fun AnalyticsScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = "Аналитика", color = NeonYellow)
        }
    }
}
