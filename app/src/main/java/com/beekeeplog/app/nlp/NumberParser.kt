package com.beekeeplog.app.nlp

import javax.inject.Inject

/**
 * Parses Russian numeral words (1–999) and digit sequences found in a string.
 * Returns a list of (value, range) pairs where range is the character span in [text].
 */
class NumberParser @Inject constructor() {

    // -------------------------------------------------------------------------
    // Tables per spec 7.1
    // -------------------------------------------------------------------------
    private val units = mapOf(
        "один" to 1, "одна" to 1, "первый" to 1, "первая" to 1, "первого" to 1, "первом" to 1,
        "два" to 2, "две" to 2, "второй" to 2, "второго" to 2, "втором" to 2,
        "три" to 3, "третий" to 3, "третьего" to 3, "третьем" to 3,
        "четыре" to 4, "четвёртый" to 4, "четвертый" to 4,
        "пять" to 5, "пятый" to 5,
        "шесть" to 6, "шестой" to 6,
        "семь" to 7, "седьмой" to 7,
        "восемь" to 8, "восьмой" to 8,
        "девять" to 9, "девятый" to 9
    )

    private val teens = mapOf(
        "десять" to 10, "десятый" to 10,
        "одиннадцать" to 11, "одиннадцатый" to 11,
        "двенадцать" to 12, "двенадцатый" to 12,
        "тринадцать" to 13, "тринадцатый" to 13,
        "четырнадцать" to 14, "четырнадцатый" to 14,
        "пятнадцать" to 15, "пятнадцатый" to 15,
        "шестнадцать" to 16, "шестнадцатый" to 16,
        "семнадцать" to 17, "семнадцатый" to 17,
        "восемнадцать" to 18, "восемнадцатый" to 18,
        "девятнадцать" to 19, "девятнадцатый" to 19
    )

    private val tens = mapOf(
        "двадцать" to 20, "двадцатый" to 20,
        "тридцать" to 30, "тридцатый" to 30,
        "сорок" to 40, "сороковой" to 40,
        "пятьдесят" to 50, "пятидесятый" to 50,
        "шестьдесят" to 60, "шестидесятый" to 60,
        "семьдесят" to 70, "семидесятый" to 70,
        "восемьдесят" to 80, "восьмидесятый" to 80,
        "девяносто" to 90, "девяностый" to 90
    )

    private val hundreds = mapOf(
        "сто" to 100, "сотый" to 100,
        "двести" to 200, "двухсотый" to 200,
        "триста" to 300, "трёхсотый" to 300, "трехсотый" to 300,
        "четыреста" to 400, "четырёхсотый" to 400,
        "пятьсот" to 500, "пятисотый" to 500,
        "шестьсот" to 600, "шестисотый" to 600,
        "семьсот" to 700, "семисотый" to 700,
        "восемьсот" to 800, "восьмисотый" to 800,
        "девятьсот" to 900, "девятисотый" to 900
    )

    private val allWords: Map<String, Int> = units + teens + tens + hundreds

    /**
     * Finds all digit sequences and Russian numeral word groups in [text].
     * Returns a list of (value, IntRange) pairs sorted by range start.
     */
    fun parse(text: String): List<Pair<Int, IntRange>> {
        val lower = text.lowercase()
        val results = mutableListOf<Pair<Int, IntRange>>()

        // 1. Digit sequences like "12", "3", "42"
        val digitRegex = Regex("""\d+""")
        for (match in digitRegex.findAll(lower)) {
            val v = match.value.toIntOrNull()
            if (v != null && v in 1..999) {
                results.add(Pair(v, match.range))
            }
        }

        // 2. Russian numeral word groups — sliding window approach
        val tokens = tokenize(lower)
        var i = 0
        while (i < tokens.size) {
            val (word, start, end) = tokens[i]

            // Try 3-token combo: hundred + ten + unit
            if (i + 2 < tokens.size) {
                val h = hundreds[word]
                val t = tens[tokens[i + 1].word]
                val u = units[tokens[i + 2].word]
                if (h != null && t != null && u != null) {
                    val rangeEnd = tokens[i + 2].endIdx
                    results.add(Pair(h + t + u, start..rangeEnd))
                    i += 3
                    continue
                }
            }
            // Try 2-token combo: hundred + unit, hundred + ten, ten + unit
            if (i + 1 < tokens.size) {
                val h = hundreds[word]
                val t = tens[word]
                val next = tokens[i + 1]
                if (h != null) {
                    val u = units[next.word] ?: tens[next.word]
                    if (u != null) {
                        results.add(Pair(h + u, start..next.endIdx))
                        i += 2
                        continue
                    }
                }
                if (t != null) {
                    val u = units[next.word]
                    if (u != null) {
                        results.add(Pair(t + u, start..next.endIdx))
                        i += 2
                        continue
                    }
                }
            }
            // Single word
            val v = allWords[word]
            if (v != null) {
                results.add(Pair(v, start..end))
            }
            i++
        }

        // Remove overlaps with digit ranges (keep digit-matched ranges)
        val digitRanges = results.filter { it.second.first < lower.length && lower[it.second.first].isDigit() }
        val wordRanges  = results.filter { it.second.first >= lower.length || !lower[it.second.first].isDigit() }

        return (digitRanges + wordRanges)
            .sortedBy { it.second.first }
            .distinctBy { it.second.first }
    }

    // -------------------------------------------------------------------------

    private data class Token(val word: String, val startIdx: Int, val endIdx: Int)

    private fun tokenize(text: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val regex = Regex("""[а-яёa-z]+""")
        for (m in regex.findAll(text)) {
            tokens.add(Token(m.value, m.range.first, m.range.last))
        }
        return tokens
    }
}
