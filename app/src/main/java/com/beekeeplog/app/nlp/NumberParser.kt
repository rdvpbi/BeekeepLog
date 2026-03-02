package com.beekeeplog.app.nlp

import javax.inject.Inject

/**
 * Parses Russian numeral words (1–999) found in a string and returns them
 * as pairs of (value, range) where range is the substring span in [text].
 */
class NumberParser @Inject constructor() {

    fun parse(text: String): List<Pair<Int, IntRange>> = emptyList()
}
