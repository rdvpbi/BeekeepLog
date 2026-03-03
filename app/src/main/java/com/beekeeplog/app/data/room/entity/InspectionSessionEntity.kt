package com.beekeeplog.app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity for the `inspection_sessions` table. */
@Entity(tableName = "inspection_sessions")
data class InspectionSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "ended_at")
    val endedAt: Long? = null,

    @ColumnInfo(name = "pause_split_ms")
    val pauseSplitMs: Long? = null,

    @ColumnInfo(name = "language_tag")
    val languageTag: String = "ru-RU",

    @ColumnInfo(name = "segments_total")
    val segmentsTotal: Int = 0,

    @ColumnInfo(name = "segments_ok")
    val segmentsOk: Int = 0,

    @ColumnInfo(name = "segments_failed")
    val segmentsFailed: Int = 0
)
