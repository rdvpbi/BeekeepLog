package com.beekeeplog.app.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beekeeplog.app.data.room.entity.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow

/** DAO for the `voice_notes` table. */
@Dao
interface VoiceNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: VoiceNoteEntity)

    /** Updates a note after NLP parsing is complete. */
    @Query("""
        UPDATE voice_notes
        SET status = :status,
            nuc_id = :nucId,
            intent = :intent,
            payload_json = :payloadJson,
            ts_end = :tsEnd,
            parse_error = :parseError
        WHERE id = :id
    """)
    suspend fun updateParsed(
        id: String,
        status: String,
        nucId: Int?,
        intent: String?,
        payloadJson: String?,
        tsEnd: Long,
        parseError: String?
    )

    @Query("SELECT * FROM voice_notes ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<VoiceNoteEntity>>

    /**
     * Returns the most recent note created after [sinceMs].
     * Used for duplicate detection (same text within 10 s).
     */
    @Query("SELECT * FROM voice_notes WHERE created_at > :sinceMs ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestSince(sinceMs: Long): VoiceNoteEntity?
}
