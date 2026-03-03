package com.beekeeplog.app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity for the `tasks` table. Represents a scheduled beekeeper task. */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "nuc_id")
    val nucId: Int,

    @ColumnInfo(name = "queen_id")
    val queenId: String?,

    @ColumnInfo(name = "task_type")
    val taskType: String,

    @ColumnInfo(name = "due_at")
    val dueAt: Long,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
