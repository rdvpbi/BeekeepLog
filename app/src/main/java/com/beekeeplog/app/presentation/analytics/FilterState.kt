package com.beekeeplog.app.presentation.analytics

import com.beekeeplog.app.domain.model.Preset
import com.beekeeplog.app.domain.model.TimeRange

/** Current filter selection state for the Analytics screen. */
data class FilterState(
    val timeRange: TimeRange = TimeRange.THIS_WEEK,
    val preset: Preset? = null
)
