package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.data.repo.SessionRepository
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.domain.model.EventType
import com.beekeeplog.app.speech.SpeechEngine
import java.util.UUID
import javax.inject.Inject

/**
 * UC-02: Stops the SpeechEngine and marks the inspection session as COMPLETED.
 * NLP parsing is handled separately by [ParseVoiceNoteUseCase] after RAW save.
 */
class StopInspectionSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val speechEngine: SpeechEngine
) {
    suspend operator fun invoke(sessionId: String) {
        speechEngine.stop()

        val now = System.currentTimeMillis()
        val session = sessionRepository.getSessionById(sessionId)
        if (session != null) {
            sessionRepository.updateSession(
                session.copy(status = "COMPLETED", endedAt = now)
            )
        }

        sessionRepository.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                eventType = EventType.SESSION_STOPPED.name,
                payloadJson = """{"sessionId":"$sessionId"}""",
                ts = now
            )
        )
    }
}
