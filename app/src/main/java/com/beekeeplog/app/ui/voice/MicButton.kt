package com.beekeeplog.app.ui.voice

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.VoicePhase
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.NeonRed

/** 150dp circular microphone button. Pulsates when LISTENING, shows emoji when IDLE. */
@Composable
fun MicButton(
    phase: VoicePhase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = phase == VoicePhase.LISTENING
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val gradient = if (isListening) {
        Brush.radialGradient(listOf(NeonRed, NeonRed.copy(alpha = 0.6f)))
    } else {
        Brush.radialGradient(listOf(NeonGreen.copy(alpha = 0.9f), NeonGreen.copy(alpha = 0.4f)))
    }

    Box(
        modifier = modifier
            .size(150.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(gradient)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .semantics {
                contentDescription = if (isListening) "Остановить запись" else "Начать запись"
            },
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            Text(
                text = "СТОП",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        } else {
            Text(
                text = "🎤",
                fontSize = 48.sp
            )
        }
    }
}
