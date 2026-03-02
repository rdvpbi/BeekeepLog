package com.beekeeplog.app.domain.model

/** An alert to be displayed in the voice screen status bar. */
data class AlertItem(
    val nucId: Int,
    val message: String,
    val severity: AlertSeverity
)
