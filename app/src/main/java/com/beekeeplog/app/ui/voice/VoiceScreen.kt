package com.beekeeplog.app.ui.voice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.beekeeplog.app.ui.theme.NeonGreen

/** Voice recording screen — 3-zone layout: status bar / recording controls / alerts. */
@Composable
fun VoiceScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = "Запись", color = NeonGreen)
        }
    }
}
