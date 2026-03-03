package com.beekeeplog.app.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.NeonRed

/** ОТМЕНА + ВЕРНО confirmation buttons. Only shown in CONFIRMING phase. */
@Composable
fun ConfirmButtons(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConfirmCircleButton(
            text = "ОТМЕНА",
            color = NeonRed,
            hapticType = HapticFeedbackType.TextHandleMove,
            onClick = onCancel,
            contentDesc = "Отменить распознанную команду"
        )
        ConfirmCircleButton(
            text = "ВЕРНО",
            color = NeonGreen,
            hapticType = HapticFeedbackType.LongPress,
            onClick = onConfirm,
            contentDesc = "Подтвердить распознанную команду"
        )
    }
}

@Composable
private fun ConfirmCircleButton(
    text: String,
    color: Color,
    hapticType: HapticFeedbackType,
    onClick: () -> Unit,
    contentDesc: String
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .clickable {
                haptic.performHapticFeedback(hapticType)
                onClick()
            }
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}
