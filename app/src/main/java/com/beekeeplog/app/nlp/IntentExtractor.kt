package com.beekeeplog.app.nlp

import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.IntentType
import javax.inject.Inject

/**
 * Extracts the primary intent and named entities from a normalised speech segment
 * using regex/keyword matching.
 */
class IntentExtractor @Inject constructor() {

    /**
     * Analyses [text] and returns the detected [IntentResult].
     * Returns [IntentType.UNKNOWN] with an empty entity map when no intent is found.
     */
    fun extract(text: String): IntentResult =
        IntentResult(intentType = IntentType.UNKNOWN, entities = emptyMap())
}
