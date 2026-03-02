package com.beekeeplog.app.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.beekeeplog.app.data.room.entity.InspectionSessionEntity
import kotlinx.coroutines.flow.Flow

/** DAO for the `inspection_sessions` table. */
@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: InspectionSessionEntity)

    @Update
    suspend fun update(session: InspectionSessionEntity)

    @Query("SELECT * FROM inspection_sessions WHERE id = :id")
    suspend fun getById(id: String): InspectionSessionEntity?

    @Query("SELECT * FROM inspection_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): InspectionSessionEntity?

    @Query("SELECT * FROM inspection_sessions ORDER BY started_at DESC")
    fun getAllFlow(): Flow<List<InspectionSessionEntity>>
}
