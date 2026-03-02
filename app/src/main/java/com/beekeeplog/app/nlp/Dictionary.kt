package com.beekeeplog.app.nlp

/** Domain-specific Russian vocabulary used by the NLP normalisation and extraction pipeline. */
object Dictionary {

    /** Known breeding line names (Carnica varieties). */
    val carnicaLines: List<String> = listOf(
        "скленар", "альфа", "бета", "гамма", "дельта", "эпсилон", "зета", "эта"
    )

    /** Known breeding line names (other breeds). */
    val otherLines: List<String> = listOf(
        "кордован", "бакфаст"
    )

    /** Confirmation trigger words. */
    val confirmWords: List<String> = listOf("верно", "да", "подтверждаю")

    /** Cancellation trigger words. */
    val cancelWords: List<String> = listOf("отмена", "нет", "отменить")

    /** Stage-related keywords. */
    val layingKeywords: List<String> = listOf("откладывает", "кладка", "матка работает")
    val virginKeywords: List<String> = listOf("девственница", "неплодная", "облёт")
    val cellKeywords: List<String> = listOf("маточник", "в ячейке")
}
