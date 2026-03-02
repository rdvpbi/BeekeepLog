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
    override suspend fun insert(task: TaskEntity): Long = taskDao.insert(task)
    override suspend fun markDone(taskId: Long) = taskDao.markDone(taskId)
    override suspend fun getOverdue(now: Long): List<TaskEntity> = taskDao.getOverdue(now)
}
