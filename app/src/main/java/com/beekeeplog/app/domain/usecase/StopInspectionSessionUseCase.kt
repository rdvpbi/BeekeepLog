package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.data.repo.SessionRepository
import com.beekeeplog.app.domain.model.EventType
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.nlp.IntentExtractor
import com.beekeeplog.app.nlp.Normalizer
import com.beekeeplog.app.segmenter.HiveDetector
import com.beekeeplog.app.speech.SpeechEngine
import java.util.UUID
import javax.inject.Inject

/**
 * UC-02: Stops the SpeechEngine, normalises and extracts intent from the session's raw text,
 * returns Pair(IntentResult, detectedNucId?).
 */
class StopInspectionSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val speechEngine: SpeechEngine,
    private val normalizer: Normalizer,
    private val intentExtractor: IntentExtractor,
    private val hiveDetector: HiveDetector
) {
    /**
     * Stops STT, runs NLP on [rawText], returns the intent result and optional nuc ID.
     * Also updates the session status to COMPLETED.
     */
    suspend operator fun invoke(sessionId: String, rawText: String):
            Pair<com.beekeeplog.app.domain.model.IntentResult, Int?> {
        speechEngine.stop()

        val normalized = normalizer.normalize(rawText)
        val intentResult = intentExtractor.extract(normalized)
        val nucId = hiveDetector.detect(normalized)

        val session = sessionRepository.getSessionById(sessionId)
        if (session != null) {
            sessionRepository.updateSession(
                session.copy(
                    status = "COMPLETED",
                    endedAt = System.currentTimeMillis()
                )
            )
        }

        sessionRepository.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                eventType = EventType.SESSION_STOPPED.name,
                payloadJson = """{"intent":"${intentResult.intentType.name}","nucId":$nucId}""",
                ts = System.currentTimeMillis()
            )
        )

        return Pair(intentResult, nucId)
    }
}
