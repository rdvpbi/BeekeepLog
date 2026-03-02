package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.domain.model.AnalyticsResult
import com.beekeeplog.app.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** UC-06: Builds the analytics screen data for the given time range filter. */
class BuildAnalyticsUseCase @Inject constructor() {
    operator fun invoke(filter: TimeRange): Flow<AnalyticsResult> =
        flowOf(AnalyticsResult(totalNucs = 0, activeQueens = 0, pendingTasks = 0, overdueTasks = 0, hives = emptyList()))
}
