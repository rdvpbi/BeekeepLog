package com.beekeeplog.app.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.AlertItem
import com.beekeeplog.app.domain.model.AlertSeverity
import com.beekeeplog.app.ui.theme.NeonOrange
import com.beekeeplog.app.ui.theme.NeonRed
import com.beekeeplog.app.ui.theme.NeonYellow
import com.beekeeplog.app.ui.theme.Surface12

/** Bottom alerts list: LazyColumn capped at 120dp, severity-styled rows. */
@Composable
fun AlertsBlock(
    alerts: List<AlertItem>,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 120.dp)
            .background(Surface12),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(alerts) { alert ->
            AlertRow(alert)
        }
    }
}

@Composable
private fun AlertRow(alert: AlertItem) {
    val (color, prefix) = when (alert.severity) {
        AlertSeverity.HIGH   -> Pair(NeonRed,    "🔴")
        AlertSeverity.MEDIUM -> Pair(NeonOrange, "🟡")
        AlertSeverity.LOW    -> Pair(NeonYellow, "⚪")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = prefix, fontSize = 12.sp)
        Text(
            text = "NUC ${alert.nucId}",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = alert.message,
            color = Color.LightGray,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}
