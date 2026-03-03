package com.beekeeplog.app.data.repo

import com.beekeeplog.app.data.room.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/** Repository interface for scheduled tasks. */
interface TaskRepository {
    fun getAllFlow(): Flow<List<TaskEntity>>
    suspend fun insert(task: TaskEntity)
    suspend fun markCompleted(taskId: String, completedAt: Long)
    suspend fun getOverdue(now: Long): List<TaskEntity>
    suspend fun getNextPendingForNuc(nucId: Int): TaskEntity?
}
