package com.beekeeplog.app.domain.model

/** Summary of database changes applied after intent confirmation. */
data class DbChangeSummary(
    val updatedNucs: Int,
    val updatedQueens: Int,
    val createdTasks: Int
)
