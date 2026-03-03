package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.segmenter.HiveDetector
import javax.inject.Inject

/** UC-04: Detects a hive ID mentioned in the given text, delegating to [HiveDetector]. */
class DetectHiveUseCase @Inject constructor(
    private val hiveDetector: HiveDetector
) {
    operator fun invoke(text: String): Int? = hiveDetector.detect(text)
}
