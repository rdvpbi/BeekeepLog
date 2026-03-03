package com.beekeeplog.app.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.KpiColor
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.NeonOrange
import com.beekeeplog.app.ui.theme.NeonRed
import com.beekeeplog.app.ui.theme.NeonYellow

/** KPI display: 72sp bold number + label. */
@Composable
fun KpiHeader(
    count: Int,
    label: String,
    kpiColor: KpiColor,
    modifier: Modifier = Modifier
) {
    val color = when (kpiColor) {
        KpiColor.GREEN  -> NeonGreen
        KpiColor.YELLOW -> NeonYellow
        KpiColor.RED    -> NeonRed
        KpiColor.ORANGE -> NeonOrange
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = count.toString(),
            color = color,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = color.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 3.sp
        )
    }
}
