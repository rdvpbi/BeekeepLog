package com.beekeeplog.app.presentation.analytics

import com.beekeeplog.app.domain.model.NucWithQueen

/** Immutable UI state for the Analytics screen. */
data class AnalyticsUiState(
    val totalNucs: Int = 0,
    val activeQueens: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val hives: List<NucWithQueen> = emptyList(),
    val isLoading: Boolean = false
)
