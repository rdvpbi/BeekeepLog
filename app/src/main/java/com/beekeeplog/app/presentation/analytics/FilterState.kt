package com.beekeeplog.app.presentation.analytics

import com.beekeeplog.app.domain.model.Genetics
import com.beekeeplog.app.domain.model.Preset
import com.beekeeplog.app.domain.model.Stage
import com.beekeeplog.app.domain.model.TimeRange

/** Current filter selection state for the Analytics screen. */
data class FilterState(
    val preset: Preset? = null,
    val genetics: Genetics? = null,
    val lineName: String? = null,
    val stage: Stage? = null,
    val timeRange: TimeRange = TimeRange.THIS_WEEK,
    val sector: String? = null,
    val row: String? = null
)
