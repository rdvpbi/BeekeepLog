package com.beekeeplog.app.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.ui.theme.NeonBlue
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.Surface1E

/**
 * Displays the live transcript during a recording session.
 * [rawBuffer] — accumulated confirmed finals (shown dimmed).
 * [streamingText] — current partial result (shown bright).
 */
@Composable
fun StreamingTranscript(
    rawBuffer: String,
    streamingText: String,
    mode: VoiceMode,
    modifier: Modifier = Modifier
) {
    val borderColor = when (mode) {
        VoiceMode.RECORD   -> NeonGreen
        VoiceMode.QUESTION -> NeonBlue
    }

    val isEmpty = rawBuffer.isBlank() && streamingText.isBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .background(Surface1E)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(borderColor)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            contentAlignment = Alignment.TopStart
        ) {
            if (isEmpty) {
                Text(
                    text = "Говорите...",
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Default,
                    lineHeight = 30.sp
                )
            } else {
                Text(
                    text = buildAnnotatedString {
                        // Accumulated text — slightly dimmed
                        if (rawBuffer.isNotBlank()) {
                            withStyle(SpanStyle(color = Color(0xFFCCCCCC))) {
                                append(rawBuffer)
                            }
                        }
                        // Current partial — bright
                        if (streamingText.isNotBlank()) {
                            if (rawBuffer.isNotBlank()) append(" ")
                            withStyle(SpanStyle(color = Color.White)) {
                                append(streamingText)
                            }
                        }
                    },
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Default,
                    lineHeight = 30.sp
                )
            }
        }
    }
}
