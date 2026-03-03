package com.beekeeplog.app.ui.voice

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.domain.model.VoicePhase
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.NeonOrange
import com.beekeeplog.app.ui.theme.NeonRed
import com.beekeeplog.app.ui.theme.NeonYellow
import com.beekeeplog.app.ui.theme.Surface12

/** Top status bar: phase indicator, mode label, optional hive badge, session timer. */
@Composable
fun StatusBar(
    phase: VoicePhase,
    mode: VoiceMode,
    currentNucId: Int?,
    sessionDurationSec: Int,
    modifier: Modifier = Modifier
) {
    val indicatorColor by animateColorAsState(
        targetValue = when (phase) {
            VoicePhase.IDLE       -> Color.Gray
            VoicePhase.LISTENING  -> NeonGreen
            VoicePhase.CONFIRMING -> NeonYellow
            VoicePhase.SUCCESS    -> NeonGreen
        },
        label = "indicator"
    )

    val phaseLabel = when (phase) {
        VoicePhase.IDLE       -> "ОЖИДАНИЕ"
        VoicePhase.LISTENING  -> "ЗАПИСЬ"
        VoicePhase.CONFIRMING -> "ПОДТВЕРЖДЕНИЕ"
        VoicePhase.SUCCESS    -> "ЗАПИСАНО"
    }

    val modeLabel = when (mode) {
        VoiceMode.RECORD   -> "ЗАПИСЬ"
        VoiceMode.QUESTION -> "ВОПРОС"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Surface12)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
                    .semantics { contentDescription = "Индикатор статуса" }
            )
            Text(
                text = phaseLabel,
                color = indicatorColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (currentNucId != null && phase == VoicePhase.LISTENING) {
            Text(
                text = "NUC $currentNucId",
                color = NeonOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.semantics { contentDescription = "Улей $currentNucId" }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = modeLabel,
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            if (phase == VoicePhase.LISTENING) {
                val mins = sessionDurationSec / 60
                val secs = sessionDurationSec % 60
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    color = NeonRed,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.semantics { contentDescription = "Таймер сессии" }
                )
            }
        }
    }
}
