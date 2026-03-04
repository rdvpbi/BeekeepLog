package com.beekeeplog.app.domain.model

/** Result of NLP parsing after a recording session is stopped. */
enum class ParseStatus {
    /** Recording is in progress, parsing not yet run. */
    NONE,
    /** Nuc ID and intent both successfully determined. */
    PARSED_OK,
    /** Text was recognised but nuc ID or intent is missing — needs clarification. */
    PARSED_PARTIAL,
    /** Intent is UNKNOWN; raw text saved as a note. */
    PARSED_FAILED
}
