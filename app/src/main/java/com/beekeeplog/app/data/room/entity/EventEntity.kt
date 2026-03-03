package com.beekeeplog.app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity for the `events` table. Append-only audit journal entry. */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String?,

    @ColumnInfo(name = "segment_id")
    val segmentId: String? = null,

    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "payload_json")
    val payloadJson: String,

    @ColumnInfo(name = "ts")
    val ts: Long
)
