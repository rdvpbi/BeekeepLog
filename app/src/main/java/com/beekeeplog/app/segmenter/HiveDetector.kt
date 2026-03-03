package com.beekeeplog.app.segmenter

import com.beekeeplog.app.nlp.NumberParser
import javax.inject.Inject

/**
 * Detects a nucleus hive ID (1–50) mentioned in a recognised speech segment.
 * Five detection patterns per spec 8.3, applied in priority order.
 */
class HiveDetector @Inject constructor(
    private val numberParser: NumberParser
) {

    /** Returns the nucleus ID (1–50) found in [text], or `null` if none is detected. */
    fun detect(text: String): Int? {
        val lower = text.lowercase().trim()

        // Pattern 1 — explicit "улей N" / "нуклеус N"
        val explicit = Regex("""(улей|нуклеус|нукл|ящик)\s+(\d+)""")
        explicit.find(lower)?.let { m ->
            val v = m.groupValues[2].toIntOrNull()
            if (v != null && v in 1..50) return v
        }

        // Pattern 2 — preposition + ordinal/cardinal: "во втором", "в третьем"
        val prep = Regex("""(?:в|во|на|у)\s+(\w+)""")
        for (m in prep.findAll(lower)) {
            val word = m.groupValues[1]
            val matches = numberParser.parse(word)
            val v = matches.firstOrNull()?.first
            if (v != null && v in 1..50) return v
        }

        // Pattern 3 — ordinal/cardinal at start of utterance
        val firstWord = lower.split(Regex("\\s+")).firstOrNull() ?: return null
        val parsed = numberParser.parse(firstWord)
        parsed.firstOrNull()?.first?.let { v ->
            if (v in 1..50) return v
        }

        // Pattern 4 — cardinal anywhere in text (word)
        val allParsed = numberParser.parse(lower)
        allParsed.filter { !lower[it.second.first].isDigit() }
            .firstOrNull()?.first?.let { v ->
            if (v in 1..50) return v
        }

        // Pattern 5 — digit sequences
        val digitMatch = Regex("""^\s*(\d+)""").find(lower)
        digitMatch?.let {
            val v = it.groupValues[1].toIntOrNull()
            if (v != null && v in 1..50) return v
        }

        return null
    }
}
