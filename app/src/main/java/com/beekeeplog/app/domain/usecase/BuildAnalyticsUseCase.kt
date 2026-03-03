package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.data.repo.NucRepository
import com.beekeeplog.app.data.room.dao.NucDao
import com.beekeeplog.app.data.room.dao.NucWithQueenTuple
import com.beekeeplog.app.domain.model.AnalyticsResult
import com.beekeeplog.app.domain.model.Genetics
import com.beekeeplog.app.domain.model.KpiColor
import com.beekeeplog.app.domain.model.LifecycleStatus
import com.beekeeplog.app.domain.model.NucWithQueen
import com.beekeeplog.app.domain.model.Preset
import com.beekeeplog.app.domain.model.Stage
import com.beekeeplog.app.presentation.analytics.FilterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * UC-09: Builds analytics screen data as a Flow, applying FilterState.
 * Delegates to NucDao JOIN queries and maps tuples to domain NucWithQueen objects.
 */
class BuildAnalyticsUseCase @Inject constructor(
    private val nucDao: NucDao
) {
    /** Returns a live [Flow] of [AnalyticsResult] for the given [filter]. */
    operator fun invoke(filter: FilterState): Flow<AnalyticsResult> {
        val sourceFlow: Flow<List<NucWithQueenTuple>> = when (filter.preset) {
            Preset.FOR_SALE -> nucDao.getForSaleFlow()
            Preset.OVERDUE  -> nucDao.getOverdueFlow(System.currentTimeMillis())
            Preset.AVRAL    -> nucDao.getAvralFlow(System.currentTimeMillis())
            null -> nucDao.getFilteredFlow(
                genetics = filter.genetics?.name,
                lineName = filter.lineName,
                stage = filter.stage?.name,
                sector = filter.sector,
                nucRow = filter.row
            )
        }

        return sourceFlow.map { tuples ->
            val nucs = tuples.map { it.toNucWithQueen() }
            val (label, color) = buildKpi(filter, nucs)
            val lines = tuples.mapNotNull { it.lineName }.distinct().sorted()
            AnalyticsResult(
                kpiCount = nucs.size,
                kpiLabel = label,
                kpiColor = color,
                nucList = nucs,
                availableLines = lines
            )
        }
    }

    private fun buildKpi(filter: FilterState, nucs: List<NucWithQueen>): Pair<String, KpiColor> =
        when (filter.preset) {
            Preset.FOR_SALE -> Pair("К ПРОДАЖЕ", KpiColor.GREEN)
            Preset.OVERDUE  -> Pair("ПРОСРОЧЕНО", KpiColor.ORANGE)
            Preset.AVRAL    -> Pair("АВРАЛЬНЫЕ", KpiColor.RED)
            null -> {
                val stageLabel = when (filter.stage) {
                    Stage.LAYING  -> "ПЛОДНЫХ"
                    Stage.VIRGIN  -> "НЕПЛОДНЫХ"
                    Stage.CELL    -> "МАТОЧНИКОВ"
                    null          -> "НУКЛЕУСОВ"
                }
                val color = if (nucs.isEmpty()) KpiColor.YELLOW else KpiColor.GREEN
                Pair(stageLabel, color)
            }
        }

    private fun NucWithQueenTuple.toNucWithQueen(): NucWithQueen {
        val now = System.currentTimeMillis()
        val changedAt = stageChangedAt ?: queenCreatedAt ?: now
        val daysInStage = ((now - changedAt) / TimeUnit.DAYS.toMillis(1)).toInt().coerceAtLeast(0)
        return NucWithQueen(
            nucId = nucId,
            sector = sector,
            row = nucRow ?: "",
            position = position ?: "",
            queenId = queenId,
            genetics = genetics?.let { runCatching { Genetics.valueOf(it) }.getOrNull() },
            lineName = lineName,
            stage = stage?.let { runCatching { Stage.valueOf(it) }.getOrNull() },
            lifecycleStatus = lifecycleStatus?.let { runCatching { LifecycleStatus.valueOf(it) }.getOrNull() },
            isElite = isElite,
            isReserved = isReserved,
            aggressionScore = aggressionScore ?: 0,
            qualityNotes = qualityNotes,
            colorMark = colorMark,
            createdAt = queenCreatedAt,
            stageChangedAt = stageChangedAt,
            daysInCurrentStage = daysInStage,
            nextTaskDescription = taskDescription,
            nextTaskDueAt = taskDueAt
        )
    }
}
