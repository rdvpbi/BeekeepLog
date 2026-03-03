package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.data.repo.SessionRepository
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.data.room.entity.InspectionSessionEntity
import com.beekeeplog.app.domain.model.EventType
import com.beekeeplog.app.speech.SpeechEngine
import java.util.UUID
import javax.inject.Inject

/**
 * UC-01: Creates a new inspection session, writes it to Room, emits SESSION_STARTED event,
 * and starts the SpeechEngine.
 */
class StartInspectionSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val speechEngine: SpeechEngine
) {
    suspend operator fun invoke(): Result<String> = runCatching {
        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val session = InspectionSessionEntity(
            id = sessionId,
            status = "ACTIVE",
            startedAt = now
        )
        sessionRepository.insertSession(session)

        sessionRepository.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                eventType = EventType.SESSION_STARTED.name,
                payloadJson = """{"sessionId":"$sessionId"}""",
                ts = now
            )
        )

        speechEngine.start()
        sessionId
    }
}
