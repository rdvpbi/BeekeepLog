package com.beekeeplog.app.ui.voice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.ui.theme.NeonBlue
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.Surface2A

/** 12-bar amplitude visualizer for the microphone input level. */
@Composable
fun AmplitudeVisualizer(
    rmsValues: List<Float>,
    mode: VoiceMode,
    modifier: Modifier = Modifier
) {
    val barColor = when (mode) {
        VoiceMode.RECORD   -> NeonGreen
        VoiceMode.QUESTION -> NeonBlue
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .semantics { contentDescription = "Визуализатор уровня звука" },
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val values = if (rmsValues.size >= 12) rmsValues else List(12) { 0f }
        values.forEachIndexed { index, raw ->
            val animated by animateFloatAsState(
                targetValue = raw.coerceIn(0.05f, 1f),
                animationSpec = tween(durationMillis = 100),
                label = "bar$index"
            )
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight(animated)
                    .background(barColor.copy(alpha = 0.7f + animated * 0.3f))
            )
        }
    }
}
