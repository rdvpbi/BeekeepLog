package com.beekeeplog.app.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beekeeplog.app.data.room.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/** DAO for the `events` table. Events are append-only; no updates or deletes. */
@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Query("SELECT * FROM events WHERE session_id = :sessionId ORDER BY ts ASC")
    fun getBySessionFlow(sessionId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY ts DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<EventEntity>
}
