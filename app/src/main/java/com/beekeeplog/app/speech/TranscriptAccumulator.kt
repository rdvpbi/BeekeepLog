package com.beekeeplog.app.speech

/**
 * Accumulates STT finals and partials across multiple short recognition windows.
 *
 * Android SpeechRecognizer works in short bursts (~5–15 s). This class stitches
 * them together into one continuous transcript for a long recording session.
 *
 * Fallback rule (1.4): if a final result is empty but a recent partial exists
 * (within [PARTIAL_FRESHNESS_MS]), the partial is used as the effective final.
 */
class TranscriptAccumulator {

    private val finalParts = mutableListOf<String>()

    @Volatile private var lastNonEmptyPartial: String = ""
    @Volatile private var lastPartialTs: Long = 0L

    /** Update with the latest partial result from STT. */
    fun onPartial(text: String) {
        if (text.isNotBlank()) {
            lastNonEmptyPartial = text
            lastPartialTs = System.currentTimeMillis()
        }
    }

    /**
     * Processes a completed recognition window result.
     * If [text] is empty but a fresh partial exists, the partial is used as fallback.
     * Returns the effective text that was added to the buffer.
     */
    fun onFinal(text: String): String {
        val effective = when {
            text.isNotBlank() -> text
            lastNonEmptyPartial.isNotBlank() &&
                    System.currentTimeMillis() - lastPartialTs <= PARTIAL_FRESHNESS_MS ->
                lastNonEmptyPartial
            else -> ""
        }
        if (effective.isNotBlank()) finalParts.add(effective)
        // Consumed — clear partial slot
        lastNonEmptyPartial = ""
        lastPartialTs = 0L
        return effective
    }

    /**
     * Assembles the full transcript from all accumulated segments.
     * Also includes a fresh partial not yet consumed by a final (e.g. at stop time).
     * Normalization applied: ё→е, multi-space collapse, trim.
     */
    fun compile(): String {
        val parts = finalParts.toMutableList()
        if (lastNonEmptyPartial.isNotBlank() &&
            System.currentTimeMillis() - lastPartialTs <= PARTIAL_FRESHNESS_MS
        ) {
            parts.add(lastNonEmptyPartial)
        }
        return parts
            .joinToString(" ")
            .replace("ё", "е")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /** Resets all state. Called at the start of each new recording session. */
    fun reset() {
        finalParts.clear()
        lastNonEmptyPartial = ""
        lastPartialTs = 0L
    }

    companion object {
        /** A partial is considered "fresh" if received within this window. */
        private const val PARTIAL_FRESHNESS_MS = 2_000L
    }
}
