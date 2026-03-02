package com.beekeeplog.app.domain.model

/** Hive detection result from the segmenter. */
data class DetectedHive(
    val nucId: Int,
    val confidence: Float?
)
