package com.beekeeplog.app.segmenter

import javax.inject.Inject

/** Detects a nucleus hive number mentioned in a recognised speech segment. */
class HiveDetector @Inject constructor() {

    /**
     * Returns the nucleus ID found in [text], or `null` if none is detected.
     */
    fun detect(text: String): Int? = null
}
