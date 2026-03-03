package com.beekeeplog.app.nlp

import javax.inject.Inject

/**
 * Normalises raw STT text using Levenshtein distance.
 * Line terms use threshold ≤1; all other terms use threshold ≤2 (spec 7.3).
 */
class Normalizer @Inject constructor() {

    /** Returns the normalised version of [text]. */
    fun normalize(text: String): String {
        val lower = text.lowercase().trim()
        val words = lower.split(Regex("\\s+"))
        return words.joinToString(" ") { word -> normalizeWord(word) }
    }

    private fun normalizeWord(word: String): String {
        // Try line terms first (stricter threshold ≤1)
        val bestLine = Dictionary.lineTerms
            .map { it to levenshtein(word, it) }
            .filter { it.second <= 1 }
            .minByOrNull { it.second }
        if (bestLine != null && bestLine.second < levenshtein(word, word)) return bestLine.first
        if (bestLine != null) return bestLine.first

        // Try all terms (threshold ≤2)
        val best = Dictionary.allTerms
            .map { it to levenshtein(word, it) }
            .filter { it.second <= 2 }
            .minByOrNull { it.second }
        return best?.first ?: word
    }

    /**
     * Standard Levenshtein distance between strings [a] and [b].
     */
    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }
}
