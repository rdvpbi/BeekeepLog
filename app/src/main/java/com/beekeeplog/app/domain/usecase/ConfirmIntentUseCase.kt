package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.data.repo.SessionRepository
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.data.room.entity.InspectionSegmentEntity
import com.beekeeplog.app.domain.model.DbChangeSummary
import com.beekeeplog.app.domain.model.IntentResult
import java.util.UUID
import javax.inject.Inject

/**
 * UC-10: Confirms the pending intent.
 * Writes segment (PENDING→OK), triggers [ApplyIntentUseCase], marks session COMPLETED,
 * emits DB_APPLIED event.
 */
class ConfirmIntentUseCase @Inject constructor(
    private val applyIntent: ApplyIntentUseCase,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        rawText: String,
        normalizedText: String,
        intentResult: IntentResult,
        nucId: Int?
    ): Result<DbChangeSummary> = runCatching {
        val now = System.currentTimeMillis()
        val segmentId = UUID.randomUUID().toString()

        // Insert confirmed segment
        sessionRepository.insertSegment(
            InspectionSegmentEntity(
                id = segmentId,
                sessionId = sessionId,
                nucId = nucId,
                rawText = rawText,
                normalizedText = normalizedText,
                intentType = intentResult.intentType.name,
                processStatus = "OK",
                closeReason = "STOP",
                createdAt = now,
                endedAt = now
            )
        )

        // Apply to DB
        val summary = applyIntent(intentResult, nucId, sessionId)

        // Mark session completed
        val session = sessionRepository.getSessionById(sessionId)
        if (session != null) {
            sessionRepository.updateSession(
                session.copy(
                    status = "COMPLETED",
                    endedAt = now,
                    segmentsTotal = session.segmentsTotal + 1,
                    segmentsOk = session.segmentsOk + 1
                )
            )
        }

        summary
    }
}
