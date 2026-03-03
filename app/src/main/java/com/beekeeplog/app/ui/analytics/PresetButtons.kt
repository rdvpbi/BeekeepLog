package com.beekeeplog.app.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.beekeeplog.app.domain.model.Preset
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.NeonOrange
import com.beekeeplog.app.ui.theme.NeonRed
import com.beekeeplog.app.ui.theme.Surface2A

/** Three preset shortcut buttons: 🔥 АВРАЛЬНЫЕ / 💰 К ПРОДАЖЕ / 🕐 ПРОСРОЧЕНО. */
@Composable
fun PresetButtons(
    activePreset: Preset?,
    onPresetChange: (Preset?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PresetBtn(
            emoji = "🔥",
            label = "АВРАЛ",
            preset = Preset.AVRAL,
            activePreset = activePreset,
            activeColor = NeonRed,
            onPresetChange = onPresetChange,
            modifier = Modifier.weight(1f)
        )
        PresetBtn(
            emoji = "💰",
            label = "ПРОДАЖА",
            preset = Preset.FOR_SALE,
            activePreset = activePreset,
            activeColor = NeonGreen,
            onPresetChange = onPresetChange,
            modifier = Modifier.weight(1f)
        )
        PresetBtn(
            emoji = "🕐",
            label = "ПРОСРОЧ",
            preset = Preset.OVERDUE,
            activePreset = activePreset,
            activeColor = NeonOrange,
            onPresetChange = onPresetChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PresetBtn(
    emoji: String,
    label: String,
    preset: Preset,
    activePreset: Preset?,
    activeColor: Color,
    onPresetChange: (Preset?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = activePreset == preset
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else Surface2A)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPresetChange(if (isActive) null else preset)
            }
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .alpha(if (isActive) 1f else 0.5f)
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Text(
            text = label,
            color = if (isActive) activeColor else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
