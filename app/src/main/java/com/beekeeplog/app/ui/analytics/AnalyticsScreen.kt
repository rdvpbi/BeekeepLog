package com.beekeeplog.app.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beekeeplog.app.presentation.analytics.AnalyticsViewModel
import com.beekeeplog.app.ui.theme.Black
import com.beekeeplog.app.ui.theme.NeonGreen

/** Analytics screen — FilterView (KPI + Preset + Chips + FAB) or ListView (Back + NucCard list). */
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        if (!uiState.isListView) {
            // Filter view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "АНАЛИТИКА",
                    color = NeonGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )

                // KPI
                KpiHeader(
                    count = uiState.kpiCount,
                    label = uiState.kpiLabel,
                    kpiColor = uiState.kpiColor
                )

                // Preset buttons
                PresetButtons(
                    activePreset = uiState.activePreset,
                    onPresetChange = viewModel::onPresetChange
                )

                // Filter chips
                FilterChipsRow(
                    filterState = filterState,
                    availableLines = uiState.availableLines,
                    onGeneticsChange = viewModel::onGeneticsChange,
                    onStageChange = viewModel::onStageChange,
                    onLineNameChange = viewModel::onLineNameChange,
                    onSectorChange = viewModel::onSectorChange
                )

                // Show list FAB
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FloatingActionButton(
                        onClick = viewModel::onShowList,
                        containerColor = NeonGreen,
                        modifier = Modifier.semantics { contentDescription = "Показать список" }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = "Список нуклеусов",
                            tint = Color.Black
                        )
                    }
                }
            }
        } else {
            // List view
            Column(modifier = Modifier.fillMaxSize()) {
                // Back header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = viewModel::onBackToFilters,
                        modifier = Modifier.semantics { contentDescription = "Назад к фильтрам" }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = NeonGreen
                        )
                    }
                    Text(
                        text = "${uiState.kpiCount} ${uiState.kpiLabel}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                NucCardList(nucs = uiState.nucList)
            }
        }
    }
}
