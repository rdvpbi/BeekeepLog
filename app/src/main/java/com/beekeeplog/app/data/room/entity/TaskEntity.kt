package com.beekeeplog.app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity for the `tasks` table. Represents a scheduled beekeeper task. */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "nuc_id")
    val nucId: Int,

    @ColumnInfo(name = "queen_id")
    val queenId: String?,

    @ColumnInfo(name = "task_type")
    val taskType: String,

    @ColumnInfo(name = "due_date")
    val dueDate: Long,

    @ColumnInfo(name = "is_done")
    val isDone: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
