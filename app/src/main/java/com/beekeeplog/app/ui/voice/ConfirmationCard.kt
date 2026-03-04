package com.beekeeplog.app.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.IntentType
import com.beekeeplog.app.domain.model.ParseStatus
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.NeonOrange
import com.beekeeplog.app.ui.theme.NeonRed
import com.beekeeplog.app.ui.theme.NeonYellow
import com.beekeeplog.app.ui.theme.Surface1E
import com.beekeeplog.app.ui.theme.Surface2A

/**
 * Card shown in CONFIRMING phase.
 * Renders different content depending on [parseStatus]:
 *   PARSED_OK      — recognised text + intent badge + nuc badge
 *   PARSED_PARTIAL — text saved, but nuc or intent is missing
 *   PARSED_FAILED  — text saved as a raw note (no structured data extracted)
 */
@Composable
fun ConfirmationCard(
    rawText: String,
    intentResult: IntentResult?,
    currentNucId: Int?,
    parseStatus: ParseStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface1E)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (parseStatus) {

            ParseStatus.PARSED_OK -> {
                Label(text = "РАСПОЗНАНО", color = NeonYellow)
                RecognizedText(rawText)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (intentResult != null) IntentBadge(intentResult.intentType)
                    if (currentNucId != null) NucBadge(currentNucId)
                }
            }

            ParseStatus.PARSED_PARTIAL -> {
                Label(text = "УТОЧНИТЕ", color = NeonOrange)
                RecognizedText(rawText)
                val hint = when {
                    currentNucId == null && intentResult?.intentType == IntentType.UNKNOWN ->
                        "Улей и действие не определены"
                    currentNucId == null ->
                        "Улей не определён — укажите вручную"
                    intentResult?.intentType == IntentType.UNKNOWN ->
                        "Действие не определено — запись сохранена как заметка"
                    else -> ""
                }
                if (hint.isNotBlank()) {
                    Text(
                        text = hint,
                        color = NeonOrange,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                if (intentResult != null && intentResult.intentType != IntentType.UNKNOWN) {
                    IntentBadge(intentResult.intentType)
                }
                if (currentNucId != null) NucBadge(currentNucId)
            }

            ParseStatus.PARSED_FAILED -> {
                Label(text = "ЗАМЕТКА", color = Color.Gray)
                if (rawText.isNotBlank()) {
                    RecognizedText(rawText)
                } else {
                    Text(
                        text = "Речь не была распознана",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = "Действие не определено. Текст сохранён как заметка.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            ParseStatus.NONE -> {
                // RAW saved, NLP pipeline is running
                Label(text = "ОБРАБОТКА...", color = Color.Gray)
                if (rawText.isNotBlank()) RecognizedText(rawText)
                Text(
                    text = "Распознаю...",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun Label(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp
    )
}

@Composable
private fun RecognizedText(text: String) {
    Text(
        text = "«$text»",
        color = Color.White,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )
}

@Composable
private fun NucBadge(nucId: Int) {
    Text(
        text = "NUC $nucId",
        color = NeonOrange,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Surface2A)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun IntentBadge(intentType: IntentType) {
    val (label, color) = when (intentType) {
        IntentType.UPDATE_STATUS_LAYING -> "ПЛОДНАЯ"     to NeonGreen
        IntentType.UPDATE_STATUS_VIRGIN -> "НЕПЛОДНАЯ"   to Color(0xFF448AFF)
        IntentType.UPDATE_STATUS_CELL   -> "МАТОЧНИК"    to NeonOrange
        IntentType.MARK_SOLD            -> "ПРОДАНА"     to NeonYellow
        IntentType.MARK_LOST            -> "ПОТЕРЯНА"    to NeonRed
        IntentType.MARK_CULLED          -> "ВЫБРАКОВАНА" to NeonRed
        IntentType.MARK_DRONE_LAYER     -> "ТРУТОВКА"    to NeonRed
        IntentType.MARK_EMPTY_SWARMED   -> "СЛЕТЕЛА"     to NeonRed
        IntentType.MARK_ELITE           -> "ЭЛИТНАЯ"     to NeonGreen
        IntentType.MARK_RESERVED        -> "РЕЗЕРВ"      to Color(0xFF448AFF)
        IntentType.SET_AGGRESSION       -> "АГРЕССИЯ"    to NeonRed
        IntentType.NEEDS_FEEDING        -> "КОРМЛЕНИЕ"   to NeonYellow
        IntentType.NEEDS_TREATMENT      -> "ОБРАБОТКА"   to NeonOrange
        IntentType.NO_CHANGES           -> "БЕЗ ИЗМЕНЕНИЙ" to Color.Gray
        else                            -> intentType.name to Color.Gray
    }
    Text(
        text = label,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
