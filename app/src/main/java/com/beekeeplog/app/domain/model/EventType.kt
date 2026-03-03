package com.beekeeplog.app.domain.model

/** Audit event type for the append-only event journal. */
enum class EventType {
    SESSION_STARTED,
    SEGMENT_CLOSED,
    NLP_PARSED,
    DB_APPLIED,
    ERROR,
    SESSION_STOPPED,
    SESSION_CANCELLED
}
