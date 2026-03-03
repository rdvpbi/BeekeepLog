package com.beekeeplog.app.ui.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.ui.theme.NeonBlue
import com.beekeeplog.app.ui.theme.NeonGreen

/** Shown after successful confirmation: "✓ ЗАПИСАНО" or "💬 ОТВЕТ". */
@Composable
fun SuccessDisplay(
    mode: VoiceMode,
    answerText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (mode == VoiceMode.RECORD) {
            Text(
                text = "✓ ЗАПИСАНО",
                color = NeonGreen,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
        } else {
            Text(
                text = "💬 ОТВЕТ",
                color = NeonBlue,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            if (answerText.isNotBlank()) {
                Text(
                    text = answerText,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
