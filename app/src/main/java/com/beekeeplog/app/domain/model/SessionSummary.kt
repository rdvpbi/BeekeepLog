package com.beekeeplog.app.domain.model

/** Summary of a completed inspection session for display. */
data class SessionSummary(
    val sessionId: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val segmentCount: Int,
    val status: SessionStatus
)
