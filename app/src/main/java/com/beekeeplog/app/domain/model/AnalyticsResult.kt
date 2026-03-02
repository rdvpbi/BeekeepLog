package com.beekeeplog.app.domain.model

/** Analytics screen data: KPI values and hive list. */
data class AnalyticsResult(
    val totalNucs: Int,
    val activeQueens: Int,
    val pendingTasks: Int,
    val overdueTasks: Int,
    val hives: List<NucWithQueen>
)
