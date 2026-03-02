package com.beekeeplog.app.domain.usecase

import javax.inject.Inject

/** UC-04: Detects a hive ID mentioned in the given text, or null if none found. */
class DetectHiveUseCase @Inject constructor() {
    operator fun invoke(text: String): Int? = null
}
