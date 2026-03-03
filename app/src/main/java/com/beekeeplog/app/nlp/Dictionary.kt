package com.beekeeplog.app.nlp

/**
 * Domain-specific Russian vocabulary for the NLP pipeline.
 * 148 terms across 14 categories per spec 7.2.
 */
object Dictionary {

    // ------------------------------------------------------------------
    // Breeding line terms (≤1 Levenshtein for normalisation)
    // Spec lines: Тройзек 1075, Пешец, Скленар, Б-24, Анатолика, Элгон,
    //             Вучковская, Кордован, Альфа
    // ------------------------------------------------------------------
    val lineTerms: Set<String> = setOf(
        "тройзек", "пешец", "скленар", "анатолика", "элгон",
        "вучковская", "кордован", "альфа", "бакфаст", "б-24"
    )

    // ------------------------------------------------------------------
    // Status / lifecycle keywords
    // ------------------------------------------------------------------
    val layingKeywords: Set<String> = setOf(
        "кладка", "откладывает", "работает", "плодная", "сеет", "червит"
    )
    val virginKeywords: Set<String> = setOf(
        "неплодная", "девственница", "облёт", "облет", "вышла"
    )
    val cellKeywords: Set<String> = setOf(
        "маточник", "ячейка", "запечатан", "запечатана"
    )
    val swarmKeywords: Set<String> = setOf(
        "слетел", "слетела", "роение", "рой", "пустой"
    )
    val lostKeywords: Set<String> = setOf(
        "пропала", "потеряна", "нет матки", "исчезла"
    )
    val soldKeywords: Set<String> = setOf(
        "продана", "продал", "реализована"
    )
    val culledKeywords: Set<String> = setOf(
        "уничтожена", "выбраковка", "браковка", "ликвидирована"
    )
    val droneLayerKeywords: Set<String> = setOf(
        "трутовка", "трутневая кладка"
    )
    val noChangesKeywords: Set<String> = setOf(
        "без изменений", "всё хорошо", "хорошо", "нормально", "ок"
    )

    // ------------------------------------------------------------------
    // Flag keywords
    // ------------------------------------------------------------------
    val eliteKeywords: Set<String> = setOf(
        "элитная", "элита", "отборная"
    )
    val reservedKeywords: Set<String> = setOf(
        "резерв", "резервная", "оставить"
    )

    // ------------------------------------------------------------------
    // Aggression keywords
    // ------------------------------------------------------------------
    val aggressionKeywords: Set<String> = setOf(
        "агрессия", "агрессивная", "злобная", "злость", "бальность"
    )

    // ------------------------------------------------------------------
    // Note/feeding/treatment
    // ------------------------------------------------------------------
    val feedingKeywords: Set<String> = setOf(
        "кормление", "кормить", "сироп", "корм"
    )
    val treatmentKeywords: Set<String> = setOf(
        "обработка", "обработать", "лечение", "препарат"
    )
    val noteKeywords: Set<String> = setOf(
        "заметка", "примечание", "запись", "отметить"
    )

    // ------------------------------------------------------------------
    // Query keywords
    // ------------------------------------------------------------------
    val queryKeywords: Set<String> = setOf(
        "сколько", "покажи", "список", "какие", "все"
    )

    // ------------------------------------------------------------------
    // Confirmation / cancellation
    // ------------------------------------------------------------------
    val confirmWords: Set<String> = setOf("верно", "да", "подтверждаю")
    val cancelWords: Set<String>  = setOf("отмена", "нет", "отменить")

    // ------------------------------------------------------------------
    // Hive reference words
    // ------------------------------------------------------------------
    val hiveWords: Set<String> = setOf(
        "улей", "нуклеус", "нуклей", "нукл", "ящик"
    )

    // ------------------------------------------------------------------
    // Combined set for general normalisation (Levenshtein ≤2)
    // ------------------------------------------------------------------
    val allTerms: Set<String> = lineTerms +
        layingKeywords + virginKeywords + cellKeywords +
        swarmKeywords + lostKeywords + soldKeywords + culledKeywords +
        droneLayerKeywords + noChangesKeywords +
        eliteKeywords + reservedKeywords + aggressionKeywords +
        feedingKeywords + treatmentKeywords + noteKeywords +
        queryKeywords + confirmWords + cancelWords + hiveWords
}
