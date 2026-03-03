package com.beekeeplog.app.nlp

import com.beekeeplog.app.domain.model.Genetics
import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.IntentType
import javax.inject.Inject

/**
 * Extracts the primary intent and named entities from a normalised speech segment
 * using regex/keyword matching per spec 7.4.
 * Priority order strictly follows spec table 7.4 (#1–#16, top-down, first match wins).
 */
class IntentExtractor @Inject constructor(
    private val numberParser: NumberParser
) {

    /**
     * Analyses [normalizedText] and returns the detected [IntentResult].
     * Patterns applied top-down per spec 7.4; first match wins.
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
                text.contains("просроч") ->
                    result(IntentType.QUERY_OVERDUE)
                text.contains("генетик") ->
                    result(IntentType.QUERY_COUNT_BY_GENETICS, extractGenetics(text))
                else ->
                    result(IntentType.QUERY_COUNT_BY_STATUS)
            }
        }

        // #1 UPDATE_STATUS_CELL — "дал маточник", "поставил маточник", etc.
        if (containsAny(text, Dictionary.cellKeywords))
            return result(IntentType.UPDATE_STATUS_CELL, extractGenetics(text))

        // #2 UPDATE_STATUS_VIRGIN — "матка вышла", "вышла", "бегает неплодка"
        if (containsAny(text, Dictionary.virginKeywords))
            return result(IntentType.UPDATE_STATUS_VIRGIN, extractGenetics(text))

        // #3 UPDATE_STATUS_LAYING — "сеет", "червит", "зачервить", "засев"
        if (containsAny(text, Dictionary.layingKeywords))
            return result(IntentType.UPDATE_STATUS_LAYING)

        // #4 MARK_SOLD — "на продажу", "клеточку", "пересылку", "забирай"
        if (containsAny(text, Dictionary.soldKeywords))
            return result(IntentType.MARK_SOLD)

        // #5 MARK_EMPTY_SWARMED — "слетела", "пусто", "вообще пусто"
        if (containsAny(text, Dictionary.swarmKeywords))
            return result(IntentType.MARK_EMPTY_SWARMED)

        // #6 MARK_LOST — "пропала", "потерялась", "не вернулась"
        if (containsAny(text, Dictionary.lostKeywords))
            return result(IntentType.MARK_LOST)

        // #7 MARK_CULLED — "давить", "в расход", "выбраковка"
        if (containsAny(text, Dictionary.culledKeywords))
            return result(IntentType.MARK_CULLED)

        // #8 MARK_DRONE_LAYER — "трутовка", "горбатый расплод"
        if (containsAny(text, Dictionary.droneLayerKeywords))
            return result(IntentType.MARK_DRONE_LAYER)

        // #9 MARK_RESERVED — "в резерв", "не продавать"
        if (containsAny(text, Dictionary.reservedKeywords))
            return result(IntentType.MARK_RESERVED)

        // #10 MARK_ELITE — "элитная", "элита"
        if (containsAny(text, Dictionary.eliteKeywords))
            return result(IntentType.MARK_ELITE)

        // #11 SET_AGGRESSION — "агрессия", "злобливость", "ужалили", "без маски"
        if (containsAny(text, Dictionary.aggressionKeywords)) {
            val score = extractAggression(text)
            val entities = mutableMapOf<String, Any?>()
            if (score != null) entities["aggression"] = score
            return IntentResult(IntentType.SET_AGGRESSION, entities)
        }

        // #12 NEEDS_FEEDING — "кормить", "канди", "сироп", "кормушку"
        if (containsAny(text, Dictionary.feedingKeywords))
            return result(IntentType.NEEDS_FEEDING)

        // #13 NEEDS_TREATMENT — "обработать", "щавелькой", "клеща"
        if (containsAny(text, Dictionary.treatmentKeywords))
            return result(IntentType.NEEDS_TREATMENT)

        // #14 NO_CHANGES — "без изменений", "норма", "порядок"
        if (containsAny(text, Dictionary.noChangesKeywords))
            return result(IntentType.NO_CHANGES)

        // #15 ADD_NOTE — "под вопрос", "крыло рваное", "больная"
        if (containsAny(text, Dictionary.noteKeywords))
            return result(IntentType.ADD_NOTE, mapOf("note" to normalizedText))

        return unknown()
    }

    /**
     * Extracts genetics type and optional line name from text per spec 7.5.
     * Checks from specific line names to general breed names (priority order).
     */
    fun extractGenetics(text: String): Map<String, Any?> {
        val entities = mutableMapOf<String, Any?>()
        val (genetics, lineName) = when {
            text.contains("тройзек")    -> Genetics.CARNICA  to "Тройзек 1075"
            text.contains("пешец")      -> Genetics.CARNICA  to "Пешец"
            text.contains("скленар")    -> Genetics.CARNICA  to "Скленар"
            text.contains("альфа")      -> Genetics.CARNICA  to "Альфа"
            text.contains("карника")    -> Genetics.CARNICA  to null
            text.contains("б-24") || text.contains("б 24") -> Genetics.BUCKFAST to "Б-24"
            text.contains("анатолика")  -> Genetics.BUCKFAST to "Анатолика"
            text.contains("элгон")      -> Genetics.BUCKFAST to "Элгон"
            text.contains("брандструп") -> Genetics.BUCKFAST to "Брандструп"
            text.contains("бакфаст")    -> Genetics.BUCKFAST to null
            text.contains("меллифера") || text.contains("среднерусская") ||
            text.contains("местная")    -> Genetics.MELLIFERA to null
            text.contains("кордован")   -> Genetics.ITALIANA to "Кордован"
            text.contains("итальянка")  -> Genetics.ITALIANA to null
            text.contains("вучковская") -> Genetics.CARPATHICA to "Вучковская"
            text.contains("карпатка") || text.contains("карпатика") -> Genetics.CARPATHICA to null
            text.contains("сахариенсис") -> Genetics.OTHER to "Сахариенсис"
            else -> null to null
        }
        if (genetics != null) entities["genetics"] = genetics.name
        if (lineName != null) entities["lineName"] = lineName
        return entities
    }

    /**
     * Extracts aggression score 0–5 from text per spec 7.6.
     * "спокойные"/"без маски" → 0; "ужалили"/"злые" → 4; else parse number.
     */
    fun extractAggression(text: String): Int? {
        if (text.contains("спокойные") || text.contains("без маски") ||
            text.contains("можно без маски") || text.contains("сидят на рамке")) return 0
        if (text.contains("ужалили") || text.contains("злые") ||
            text.contains("злая зараза")) return 4
        val nums = numberParser.parse(text)
        return nums.map { it.first }.firstOrNull { it in 0..5 }
    }

    // -------------------------------------------------------------------------

    private fun result(type: IntentType, entities: Map<String, Any?> = emptyMap()) =
        IntentResult(intentType = type, entities = entities)

    private fun unknown() = IntentResult(IntentType.UNKNOWN, emptyMap())

    private fun containsAny(text: String, keywords: Set<String>): Boolean =
        keywords.any { text.contains(it) }
}
