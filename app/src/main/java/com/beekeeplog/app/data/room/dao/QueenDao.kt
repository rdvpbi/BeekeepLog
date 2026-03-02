package com.beekeeplog.app.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.beekeeplog.app.data.room.entity.QueenEntity
import kotlinx.coroutines.flow.Flow

/** DAO for the `queens` table. */
@Dao
interface QueenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(queen: QueenEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(queens: List<QueenEntity>)

    @Update
    suspend fun update(queen: QueenEntity)

    @Query("SELECT * FROM queens ORDER BY id ASC")
    fun getAllFlow(): Flow<List<QueenEntity>>

    @Query("SELECT * FROM queens WHERE id = :id")
    suspend fun getById(id: String): QueenEntity?

    @Query("SELECT * FROM queens WHERE nuc_id = :nucId LIMIT 1")
    suspend fun getByNucId(nucId: Int): QueenEntity?

    @Query("UPDATE queens SET stage = :stage, updated_at = :updatedAt WHERE id = :queenId")
    suspend fun updateStage(queenId: String, stage: String, updatedAt: Long)

    @Query("UPDATE queens SET lifecycle_status = :status, updated_at = :updatedAt WHERE id = :queenId")
    suspend fun updateLifecycleStatus(queenId: String, status: String, updatedAt: Long)
}
