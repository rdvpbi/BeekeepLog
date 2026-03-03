package com.beekeeplog.app.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.ui.theme.NeonBlue
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.Surface2A

/** ЗАПИСЬ / ВОПРОС mode toggle. Only enabled in IDLE phase. */
@Composable
fun ModeSwitcher(
    currentMode: VoiceMode,
    onModeChange: (VoiceMode) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Surface2A)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeTab(
            label = "ЗАПИСЬ",
            selected = currentMode == VoiceMode.RECORD,
            activeColor = NeonGreen,
            enabled = enabled,
            onClick = { onModeChange(VoiceMode.RECORD) },
            contentDesc = "Режим записи"
        )
        ModeTab(
            label = "ВОПРОС",
            selected = currentMode == VoiceMode.QUESTION,
            activeColor = NeonBlue,
            enabled = enabled,
            onClick = { onModeChange(VoiceMode.QUESTION) },
            contentDesc = "Режим вопроса"
        )
    }
}

@Composable
private fun ModeTab(
    label: String,
    selected: Boolean,
    activeColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    contentDesc: String
) {
    val haptic = LocalHapticFeedback.current
    Text(
        text = label,
        color = if (selected) activeColor else Color.Gray,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { contentDescription = contentDesc }
    )
}
