package com.beekeeplog.app.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.beekeeplog.app.domain.model.NucWithQueen
import com.beekeeplog.app.domain.model.Stage
import com.beekeeplog.app.ui.theme.NeonBlue
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.NeonOrange
import com.beekeeplog.app.ui.theme.NeonRed
import com.beekeeplog.app.ui.theme.NeonYellow
import com.beekeeplog.app.ui.theme.Surface1E
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Single nuc card with left border by stage, queen data, next task. */
@Composable
fun NucCard(
    nuc: NucWithQueen,
    modifier: Modifier = Modifier
) {
    val stageColor = when (nuc.stage) {
        Stage.LAYING -> NeonGreen
        Stage.VIRGIN -> NeonBlue
        Stage.CELL   -> NeonOrange
        null         -> Color.Gray
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Surface1E)
    ) {
        // Left colored border by stage
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(stageColor)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header row: NUC id + sector + flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NUC ${nuc.nucId}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (nuc.isElite)    FlagBadge("★", NeonYellow)
                    if (nuc.isReserved) FlagBadge("R", NeonBlue)
                    val agg = nuc.aggressionScore
                    if (agg > 0) FlagBadge("A$agg", NeonRed)
                }
            }

            // Genetics + line
            val geneticsText = buildString {
                append(nuc.genetics?.name ?: "—")
                if (!nuc.lineName.isNullOrBlank()) append(" · ${nuc.lineName}")
            }
            Text(
                text = geneticsText,
                color = stageColor.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            // Stage + days
            val stageLabel = when (nuc.stage) {
                Stage.LAYING -> "Плодная"
                Stage.VIRGIN -> "Неплодная"
                Stage.CELL   -> "Маточник"
                null         -> "Пусто"
            }
            Text(
                text = "$stageLabel · ${nuc.daysInCurrentStage} дн",
                color = Color.Gray,
                fontSize = 11.sp
            )

            // Next task
            val taskDesc = nuc.nextTaskDescription
            val taskDue  = nuc.nextTaskDueAt
            if (taskDesc != null && taskDue != null) {
                val now = System.currentTimeMillis()
                val taskColor = if (taskDue < now) NeonRed else NeonYellow
                val dateStr = SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(taskDue))
                Text(
                    text = "⏰ $dateStr $taskDesc",
                    color = taskColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FlagBadge(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}
