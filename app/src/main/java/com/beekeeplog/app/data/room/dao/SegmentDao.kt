package com.beekeeplog.app.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.beekeeplog.app.data.room.entity.InspectionSegmentEntity
import kotlinx.coroutines.flow.Flow

/** DAO for the `inspection_segments` table. */
@Dao
interface SegmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(segment: InspectionSegmentEntity): Long

    @Update
    suspend fun update(segment: InspectionSegmentEntity)

    @Query("SELECT * FROM inspection_segments WHERE session_id = :sessionId ORDER BY created_at ASC")
    fun getBySessionFlow(sessionId: String): Flow<List<InspectionSegmentEntity>>

    @Query("SELECT * FROM inspection_segments WHERE id = :id")
    suspend fun getById(id: Long): InspectionSegmentEntity?
}
