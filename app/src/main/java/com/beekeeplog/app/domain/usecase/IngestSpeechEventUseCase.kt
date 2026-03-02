package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.speech.SpeechEvent
import javax.inject.Inject

/** UC-03: Processes an incoming speech event within the current session. */
class IngestSpeechEventUseCase @Inject constructor() {
    suspend operator fun invoke(sessionId: String, event: SpeechEvent) { /* stub */ }
}
