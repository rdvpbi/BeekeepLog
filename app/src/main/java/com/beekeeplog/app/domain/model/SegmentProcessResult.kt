package com.beekeeplog.app.domain.model

/** Result of processing a single inspection segment through the NLP pipeline. */
data class SegmentProcessResult(
    val segmentId: String,
    val nucId: Int?,
    val intentResult: IntentResult,
    val processStatus: ProcessStatus
)
