package com.beekeeplog.app.domain.usecase

import javax.inject.Inject

/** UC-02: Marks the given session as completed. */
class StopInspectionSessionUseCase @Inject constructor() {
    suspend operator fun invoke(sessionId: String) { /* stub */ }
}
