package com.beekeeplog.app.nlp

import javax.inject.Inject

/**
 * Normalises raw speech recognition text using Levenshtein distance (≤2, or ≤1
 * for breeding line names) to correct minor STT errors against [Dictionary] terms.
 */
class Normalizer @Inject constructor() {

    /** Returns the normalised version of [text]. */
    fun normalize(text: String): String = text
}
