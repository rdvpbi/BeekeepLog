package com.beekeeplog.app.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.beekeeplog.app.data.room.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/** DAO for the `tasks` table. */
@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY due_at ASC")
    fun getAllFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE nuc_id = :nucId AND is_completed = 0 ORDER BY due_at ASC")
    fun getPendingByNucFlow(nucId: Int): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET is_completed = 1, completed_at = :completedAt WHERE id = :taskId")
    suspend fun markCompleted(taskId: String, completedAt: Long)

    @Query("SELECT * FROM tasks WHERE due_at < :now AND is_completed = 0")
    suspend fun getOverdue(now: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE nuc_id = :nucId AND is_completed = 0 ORDER BY due_at ASC LIMIT 1")
    suspend fun getNextPendingForNuc(nucId: Int): TaskEntity?

    @Query("UPDATE tasks SET is_completed = 1, completed_at = :completedAt WHERE queen_id = :queenId AND is_completed = 0")
    suspend fun completeAllForQueen(queenId: String, completedAt: Long)

    @Query("UPDATE tasks SET is_completed = 1, completed_at = :completedAt WHERE nuc_id = :nucId AND task_type = :taskType AND is_completed = 0")
    suspend fun completeByTypeForNuc(nucId: Int, taskType: String, completedAt: Long)
}
