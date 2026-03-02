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
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY due_date ASC")
    fun getAllFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE nuc_id = :nucId AND is_done = 0 ORDER BY due_date ASC")
    fun getPendingByNucFlow(nucId: Int): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET is_done = 1 WHERE id = :taskId")
    suspend fun markDone(taskId: Long)

    @Query("SELECT * FROM tasks WHERE due_date < :now AND is_done = 0")
    suspend fun getOverdue(now: Long): List<TaskEntity>
}
