package com.beekeeplog.app.domain.model

/** Combined nucleus + queen data for UI display (result of a JOIN query). */
data class NucWithQueen(
    val nucId: Int,
    val sector: String,
    val row: Int,
    val position: Int,
    val queenId: String?,
    val genetics: Genetics?,
    val lineName: String?,
    val stage: Stage?,
    val lifecycleStatus: LifecycleStatus?,
    val isElite: Boolean,
    val isReserved: Boolean,
    val aggressionScore: Int
)
