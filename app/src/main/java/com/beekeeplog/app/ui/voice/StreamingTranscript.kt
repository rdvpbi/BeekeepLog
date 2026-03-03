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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.ui.theme.NeonBlue
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.Surface1E

/** Displays the live streaming transcript with a 3dp left border in mode color. */
@Composable
fun StreamingTranscript(
    text: String,
    mode: VoiceMode,
    modifier: Modifier = Modifier
) {
    val borderColor = when (mode) {
        VoiceMode.RECORD   -> NeonGreen
        VoiceMode.QUESTION -> NeonBlue
    }

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
            Text(
                text = if (text.isBlank()) "Говорите..." else text,
                color = if (text.isBlank()) Color.Gray else Color.White,
                fontSize = 24.sp,
                fontFamily = FontFamily.Default,
                lineHeight = 30.sp
            )
        }
    }
}
