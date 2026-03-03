package com.beekeeplog.app.domain.model

/** Analytics screen data: KPI values and hive list. */
data class AnalyticsResult(
    val kpiCount: Int,
    val kpiLabel: String,
    val kpiColor: KpiColor,
    val nucList: List<NucWithQueen>,
    val availableLines: List<String>
)
