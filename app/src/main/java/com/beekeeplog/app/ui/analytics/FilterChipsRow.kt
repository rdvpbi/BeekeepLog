package com.beekeeplog.app.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beekeeplog.app.domain.model.Genetics
import com.beekeeplog.app.domain.model.Stage
import com.beekeeplog.app.presentation.analytics.FilterState
import com.beekeeplog.app.ui.theme.NeonBlue
import com.beekeeplog.app.ui.theme.NeonGreen
import com.beekeeplog.app.ui.theme.NeonOrange
import com.beekeeplog.app.ui.theme.NeonYellow

/** Filter chips: Genetics, Stage, Lines (animated), Sector, Row. */
@Composable
fun FilterChipsRow(
    filterState: FilterState,
    availableLines: List<String>,
    onGeneticsChange: (Genetics?) -> Unit,
    onStageChange: (Stage?) -> Unit,
    onLineNameChange: (String?) -> Unit,
    onSectorChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Genetics chips
        ChipRow(label = "Генетика") {
            listOf(
                Genetics.CARNICA to "Карника",
                Genetics.BUCKFAST to "Бакфаст",
                Genetics.ITALIANA to "Итальянка",
                Genetics.CARPATHICA to "Карпатка",
                Genetics.MELLIFERA to "Тёмная"
            ).forEach { (g, name) ->
                SelectableChip(
                    label = name,
                    selected = filterState.genetics == g,
                    activeColor = NeonGreen,
                    onClick = { onGeneticsChange(if (filterState.genetics == g) null else g) }
                )
            }
        }

        // Stage chips
        ChipRow(label = "Стадия") {
            listOf(
                Stage.LAYING to "Плодная",
                Stage.VIRGIN to "Неплодная",
                Stage.CELL   to "Маточник"
            ).forEach { (s, name) ->
                SelectableChip(
                    label = name,
                    selected = filterState.stage == s,
                    activeColor = NeonYellow,
                    onClick = { onStageChange(if (filterState.stage == s) null else s) }
                )
            }
        }

        // Line chips — cascaded visibility when genetics selected
        AnimatedVisibility(visible = filterState.genetics != null && availableLines.isNotEmpty()) {
            ChipRow(label = "Линия") {
                availableLines.forEach { line ->
                    SelectableChip(
                        label = line,
                        selected = filterState.lineName == line,
                        activeColor = NeonBlue,
                        onClick = { onLineNameChange(if (filterState.lineName == line) null else line) }
                    )
                }
            }
        }

        // Sector chips
        ChipRow(label = "Сектор") {
            listOf("A", "B", "C", "D").forEach { sector ->
                SelectableChip(
                    label = sector,
                    selected = filterState.sector == sector,
                    activeColor = NeonOrange,
                    onClick = { onSectorChange(if (filterState.sector == sector) null else sector) }
                )
            }
        }
    }
}

@Composable
private fun ChipRow(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label.uppercase(),
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    FilterChip(
        selected = selected,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = activeColor.copy(alpha = 0.2f),
            selectedLabelColor = activeColor,
            labelColor = Color.Gray
        )
    )
}
