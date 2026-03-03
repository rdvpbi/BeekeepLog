package com.beekeeplog.app.data.repo

import com.beekeeplog.app.data.room.dao.TaskDao
import com.beekeeplog.app.data.room.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** [TaskRepository] implementation backed by [TaskDao]. */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllFlow(): Flow<List<TaskEntity>> = taskDao.getAllFlow()
    override suspend fun insert(task: TaskEntity) { taskDao.insert(task) }
    override suspend fun markCompleted(taskId: String, completedAt: Long) = taskDao.markCompleted(taskId, completedAt)
    override suspend fun getOverdue(now: Long): List<TaskEntity> = taskDao.getOverdue(now)
    override suspend fun getNextPendingForNuc(nucId: Int): TaskEntity? = taskDao.getNextPendingForNuc(nucId)
}
