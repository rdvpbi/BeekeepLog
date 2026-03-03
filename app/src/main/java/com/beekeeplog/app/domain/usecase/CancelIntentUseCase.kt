package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.data.repo.SessionRepository
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.data.room.entity.InspectionSegmentEntity
import com.beekeeplog.app.domain.model.EventType
import java.util.UUID
import javax.inject.Inject

/**
 * UC-11: Cancels the pending intent.
 * Writes segment (FAILED), marks session COMPLETED with failed=1, emits SESSION_CANCELLED event.
 */
class CancelIntentUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        rawText: String,
        nucId: Int?
    ) {
        val now = System.currentTimeMillis()
        val segmentId = UUID.randomUUID().toString()

        sessionRepository.insertSegment(
            InspectionSegmentEntity(
                id = segmentId,
                sessionId = sessionId,
                nucId = nucId,
                rawText = rawText,
                normalizedText = null,
                intentType = null,
                processStatus = "FAILED",
                closeReason = "STOP",
                createdAt = now,
                endedAt = now
            )
        )

        val session = sessionRepository.getSessionById(sessionId)
        if (session != null) {
            sessionRepository.updateSession(
                session.copy(
                    status = "COMPLETED",
                    endedAt = now,
                    segmentsTotal = session.segmentsTotal + 1,
                    segmentsFailed = session.segmentsFailed + 1
                )
            )
        }

        sessionRepository.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                segmentId = segmentId,
                eventType = EventType.SESSION_CANCELLED.name,
                payloadJson = """{"sessionId":"$sessionId","nucId":$nucId}""",
                ts = now
            )
        )
    }
}
