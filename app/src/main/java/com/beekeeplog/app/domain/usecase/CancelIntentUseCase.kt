package com.beekeeplog.app.domain.usecase

import javax.inject.Inject

/** UC-11: Cancels the pending intent and clears the confirmation state. */
class CancelIntentUseCase @Inject constructor() {
    suspend operator fun invoke(sessionId: String) { /* stub */ }
}
