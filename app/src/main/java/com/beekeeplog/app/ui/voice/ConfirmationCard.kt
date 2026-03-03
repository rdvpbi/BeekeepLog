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
import com.beekeeplog.app.ui.theme.NeonYellow
import com.beekeeplog.app.ui.theme.Surface1E
import com.beekeeplog.app.ui.theme.Surface2A

/** Card shown in CONFIRMING phase — displays recognised text and intent badge. */
@Composable
fun ConfirmationCard(
    recognizedText: String,
    intentResult: IntentResult,
    currentNucId: Int?,
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
        Text(
            text = "РАСПОЗНАНО",
            color = NeonYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Text(
            text = "«$recognizedText»",
            color = Color.White,
            fontSize = 18.sp,
            lineHeight = 24.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IntentBadge(intentResult.intentType)
            if (currentNucId != null) {
                Text(
                    text = "NUC $currentNucId",
                    color = Color(0xFFFF9100),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Surface2A)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun IntentBadge(intentType: IntentType) {
    val (label, color) = when (intentType) {
        IntentType.UPDATE_STATUS_LAYING  -> Pair("ПЛОДНАЯ", Color(0xFF00FF41))
        IntentType.UPDATE_STATUS_VIRGIN  -> Pair("НЕПЛОДНАЯ", Color(0xFF448AFF))
        IntentType.UPDATE_STATUS_CELL    -> Pair("МАТОЧНИК", Color(0xFFFF9100))
        IntentType.MARK_SOLD             -> Pair("ПРОДАНА", Color(0xFFFFD600))
        IntentType.MARK_LOST             -> Pair("ПОТЕРЯНА", Color(0xFFFF1744))
        IntentType.MARK_CULLED           -> Pair("ВЫБРАКОВАНА", Color(0xFFFF1744))
        IntentType.MARK_ELITE            -> Pair("ЭЛИТНАЯ", Color(0xFF00FF41))
        IntentType.MARK_RESERVED         -> Pair("РЕЗЕРВ", Color(0xFF448AFF))
        IntentType.SET_AGGRESSION        -> Pair("АГРЕССИЯ", Color(0xFFFF1744))
        IntentType.NEEDS_FEEDING         -> Pair("КОРМЛЕНИЕ", Color(0xFFFFD600))
        IntentType.NEEDS_TREATMENT       -> Pair("ОБРАБОТКА", Color(0xFFFF9100))
        IntentType.NO_CHANGES            -> Pair("БЕЗ ИЗМЕНЕНИЙ", Color.Gray)
        else                             -> Pair(intentType.name, Color.Gray)
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
