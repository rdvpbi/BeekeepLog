package com.beekeeplog.app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity for the `queens` table. Represents one queen bee record. */
@Entity(tableName = "queens")
data class QueenEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "genetics")
    val genetics: String,

    @ColumnInfo(name = "line_name")
    val lineName: String?,

    @ColumnInfo(name = "stage")
    val stage: String,

    @ColumnInfo(name = "lifecycle_status")
    val lifecycleStatus: String,

    @ColumnInfo(name = "nuc_id")
    val nucId: Int?,

    @ColumnInfo(name = "is_elite")
    val isElite: Boolean,

    @ColumnInfo(name = "is_reserved")
    val isReserved: Boolean,

    @ColumnInfo(name = "aggression_score")
    val aggressionScore: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
