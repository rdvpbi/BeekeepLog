package com.beekeeplog.app.domain.model

/** Time range filter for task scheduling views. */
enum class TimeRange {
    TODAY,
    TOMORROW,
    THIS_WEEK,
    NEXT_WEEK,
    OVERDUE
}
