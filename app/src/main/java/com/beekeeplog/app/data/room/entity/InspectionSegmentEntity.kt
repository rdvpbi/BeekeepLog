package com.beekeeplog.app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity for the `inspection_segments` table. Represents one parsed voice segment. */
@Entity(tableName = "inspection_segments")
data class InspectionSegmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "nuc_id")
    val nucId: Int?,

    @ColumnInfo(name = "raw_text")
    val rawText: String,

    @ColumnInfo(name = "normalized_text")
    val normalizedText: String?,

    @ColumnInfo(name = "intent_type")
    val intentType: String?,

    @ColumnInfo(name = "process_status")
    val processStatus: String,

    @ColumnInfo(name = "close_reason")
    val closeReason: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
