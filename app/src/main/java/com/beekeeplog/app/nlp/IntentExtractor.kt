package com.beekeeplog.app.nlp

import com.beekeeplog.app.domain.model.Genetics
import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.IntentType
import javax.inject.Inject

/**
 * Extracts the primary intent and named entities from a normalised speech segment
 * using regex/keyword matching per spec 7.4.
 */
class IntentExtractor @Inject constructor(
    private val numberParser: NumberParser
) {

    /**
     * Analyses [normalizedText] and returns the detected [IntentResult].
     * Patterns applied top-down; first match wins.
     */
    fun extract(normalizedText: String): IntentResult {
        val text = normalizedText.lowercase().trim()
        if (text.isBlank()) return unknown()

        // --- Queries (highest priority) ---
        if (containsAny(text, Dictionary.queryKeywords)) {
            return when {
                text.contains("готов") || text.contains("продаж") ->
                    result(IntentType.QUERY_READY_FOR_SALE)
                text.contains("пуст") ->
                    result(IntentType.QUERY_EMPTY_NUCS)
                text.contains("просроч") || text.contains("просрочен") ->
                    result(IntentType.QUERY_OVERDUE)
                text.contains("генетик") ->
                    result(IntentType.QUERY_COUNT_BY_GENETICS, extractGenetics(text))
                else ->
                    result(IntentType.QUERY_COUNT_BY_STATUS)
            }
        }

        // --- Status transitions ---
        if (containsAny(text, Dictionary.cellKeywords))    return result(IntentType.UPDATE_STATUS_CELL)
        if (containsAny(text, Dictionary.virginKeywords))  return result(IntentType.UPDATE_STATUS_VIRGIN)
        if (containsAny(text, Dictionary.layingKeywords))  return result(IntentType.UPDATE_STATUS_LAYING)

        // --- Lifecycle marks ---
        if (containsAny(text, Dictionary.swarmKeywords))   return result(IntentType.MARK_EMPTY_SWARMED)
        if (containsAny(text, Dictionary.lostKeywords))    return result(IntentType.MARK_LOST)
        if (containsAny(text, Dictionary.soldKeywords))    return result(IntentType.MARK_SOLD)
        if (containsAny(text, Dictionary.culledKeywords))  return result(IntentType.MARK_CULLED)
        if (containsAny(text, Dictionary.droneLayerKeywords)) return result(IntentType.MARK_DRONE_LAYER)

        // --- Flags ---
        if (containsAny(text, Dictionary.eliteKeywords))   return result(IntentType.MARK_ELITE)
        if (containsAny(text, Dictionary.reservedKeywords)) return result(IntentType.MARK_RESERVED)

        // --- Aggression ---
        if (containsAny(text, Dictionary.aggressionKeywords)) {
            val score = extractAggression(text)
            val entities = mutableMapOf<String, Any?>()
            if (score != null) entities["aggression"] = score
            return IntentResult(IntentType.SET_AGGRESSION, entities)
        }

        // --- Feeding / Treatment / Note ---
        if (containsAny(text, Dictionary.feedingKeywords))    return result(IntentType.NEEDS_FEEDING)
        if (containsAny(text, Dictionary.treatmentKeywords))  return result(IntentType.NEEDS_TREATMENT)
        if (containsAny(text, Dictionary.noteKeywords))       return result(IntentType.ADD_NOTE)

        // --- No changes ---
        if (containsAny(text, Dictionary.noChangesKeywords)) return result(IntentType.NO_CHANGES)

        return unknown()
    }

    /**
     * Extracts genetics type and optional line name from text per spec 7.5.
     */
    fun extractGenetics(text: String): Map<String, Any?> {
        val entities = mutableMapOf<String, Any?>()
        val genetics = when {
            text.contains("карника") || text.contains("скленар") ||
            text.contains("альфа") || text.contains("элгон") ||
            text.contains("анатолика") || text.contains("вучковская") -> Genetics.CARNICA
            text.contains("бакфаст") || text.contains("тройзек") ||
            text.contains("пешец") || text.contains("б-24") -> Genetics.BUCKFAST
            text.contains("кордован") -> Genetics.ITALIANA
            text.contains("карпатка") || text.contains("карпатика") -> Genetics.CARPATHICA
            text.contains("среднерусская") || text.contains("темная") ||
            text.contains("тёмная") -> Genetics.MELLIFERA
            else -> null
        }
        if (genetics != null) entities["genetics"] = genetics.name
        // Extract line name
        val lineName = Dictionary.lineTerms.firstOrNull { text.contains(it) }
        if (lineName != null) entities["lineName"] = lineName
        return entities
    }

    /**
     * Extracts aggression score 1–5 from text per spec 7.6.
     */
    fun extractAggression(text: String): Int? {
        val nums = numberParser.parse(text)
        return nums.map { it.first }.firstOrNull { it in 1..5 }
    }

    // -------------------------------------------------------------------------

    private fun result(type: IntentType, entities: Map<String, Any?> = emptyMap()) =
        IntentResult(intentType = type, entities = entities)

    private fun unknown() = IntentResult(IntentType.UNKNOWN, emptyMap())

    private fun containsAny(text: String, keywords: Set<String>): Boolean =
        keywords.any { text.contains(it) }
}
