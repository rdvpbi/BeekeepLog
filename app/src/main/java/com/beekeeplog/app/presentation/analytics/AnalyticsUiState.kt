package com.beekeeplog.app.presentation.analytics

import com.beekeeplog.app.domain.model.KpiColor
import com.beekeeplog.app.domain.model.NucWithQueen
import com.beekeeplog.app.domain.model.Preset

/** Immutable UI state for the Analytics screen. */
data class AnalyticsUiState(
    val kpiCount: Int = 0,
    val kpiLabel: String = "",
    val kpiColor: KpiColor = KpiColor.GREEN,
    val activePreset: Preset? = null,
    val nucList: List<NucWithQueen> = emptyList(),
    val isListView: Boolean = false,
    val availableLines: List<String> = emptyList(),
    val isLoading: Boolean = false
)
