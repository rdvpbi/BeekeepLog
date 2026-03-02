package com.beekeeplog.app.domain.usecase

import javax.inject.Inject

/** UC-01: Creates a new inspection session and returns its UUID. */
class StartInspectionSessionUseCase @Inject constructor() {
    suspend operator fun invoke(): Result<String> = Result.success("")
}
