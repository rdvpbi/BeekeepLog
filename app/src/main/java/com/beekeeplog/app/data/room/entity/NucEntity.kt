package com.beekeeplog.app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity for the `nucs` table. Represents one nucleus hive. */
@Entity(tableName = "nucs")
data class NucEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "sector")
    val sector: String,

    @ColumnInfo(name = "row")
    val row: Int,

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "current_queen_id")
    val currentQueenId: String?
)
