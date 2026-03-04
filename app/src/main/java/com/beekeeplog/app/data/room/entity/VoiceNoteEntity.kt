package com.beekeeplog.app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists one beekeeper dictation entry in two logical layers:
 *
 *   RAW layer  — always written immediately when recording stops:
 *                [rawText], [tsStart], [tsEnd], [status]=RAW_SAVED
 *
 *   PARSE layer — updated after NLP pipeline runs (may stay null if parse fails):
 *                [nucId], [intent], [payloadJson], [parseError]
 *
 * Status values: RAW_SAVED | PARSED_OK | PARSED_PARTIAL | PARSED_FAILED
 * parseError values: EMPTY | NO_HIVE | NO_INTENT | AMBIGUOUS | DUPLICATE
 */
@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "ts_start")
    val tsStart: Long,

    @ColumnInfo(name = "ts_end")
    val tsEnd: Long? = null,

    @ColumnInfo(name = "raw_text")
    val rawText: String,

    /** RAW_SAVED | PARSED_OK | PARSED_PARTIAL | PARSED_FAILED */
    @ColumnInfo(name = "status")
    val status: String,

    /** EMPTY | NO_HIVE | NO_INTENT | AMBIGUOUS | DUPLICATE — set when status != PARSED_OK */
    @ColumnInfo(name = "parse_error")
    val parseError: String? = null,

    @ColumnInfo(name = "nuc_id")
    val nucId: Int? = null,

    @ColumnInfo(name = "intent")
    val intent: String? = null,

    /** JSON string of extracted entities (aggression score, genetics, etc.) */
    @ColumnInfo(name = "payload_json")
    val payloadJson: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
