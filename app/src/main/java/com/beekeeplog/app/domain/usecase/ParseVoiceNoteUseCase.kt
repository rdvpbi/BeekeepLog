package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.data.room.dao.VoiceNoteDao
import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.IntentType
import com.beekeeplog.app.domain.model.ParseStatus
import com.beekeeplog.app.nlp.IntentExtractor
import com.beekeeplog.app.nlp.Normalizer
import com.beekeeplog.app.segmenter.HiveDetector
import javax.inject.Inject

/**
 * Runs the full NLP pipeline on a [VoiceNoteEntity] and updates its parse result in Room.
 *
 * Pipeline: normalize → hive detection → intent extraction → entities
 *
 * Status rules (spec 2.4):
 *   PARSED_OK      — nucId found AND intent != UNKNOWN
 *   PARSED_PARTIAL — text present but nucId missing OR intent UNKNOWN
 *   PARSED_FAILED  — nothing extracted (blank text, or UNKNOWN + no nuc)
 *
 * If nucId is not found in text but [lastKnownNucId] is fresh (provided by caller),
 * it is used as a fallback → PARSED_PARTIAL (since it is assumed, not confirmed).
 */
class ParseVoiceNoteUseCase @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao,
    private val normalizer: Normalizer,
    private val hiveDetector: HiveDetector,
    private val intentExtractor: IntentExtractor
) {
    data class ParseResult(
        val parseStatus: ParseStatus,
        val intentResult: IntentResult,
        val nucId: Int?,
        val normalizedText: String
    )

    suspend operator fun invoke(
        noteId: String,
        rawText: String,
        lastKnownNucId: Int? = null
    ): ParseResult {
        val normalized = normalizer.normalize(rawText)
        val intentResult = intentExtractor.extract(normalized)
        val detectedNuc = hiveDetector.detect(normalized)

        // Use detected nuc; fall back to last-known if caller provides it
        val nucId = detectedNuc ?: lastKnownNucId

        val parseStatus = when {
            rawText.isBlank() ->
                ParseStatus.PARSED_FAILED
            intentResult.intentType == IntentType.UNKNOWN && nucId == null ->
                ParseStatus.PARSED_FAILED
            detectedNuc == null || intentResult.intentType == IntentType.UNKNOWN ->
                ParseStatus.PARSED_PARTIAL
            else ->
                ParseStatus.PARSED_OK
        }

        val parseError = when (parseStatus) {
            ParseStatus.PARSED_FAILED   ->
                if (rawText.isBlank()) "EMPTY" else "AMBIGUOUS"
            ParseStatus.PARSED_PARTIAL  -> when {
                detectedNuc == null && intentResult.intentType == IntentType.UNKNOWN -> "NO_HIVE,NO_INTENT"
                detectedNuc == null -> "NO_HIVE"
                else                -> "NO_INTENT"
            }
            else -> null
        }

        val payloadJson = if (intentResult.entities.isNotEmpty())
            intentResult.entities.entries.joinToString(",", "{", "}") {
                "\"${it.key}\":\"${it.value}\""
            }
        else null

        voiceNoteDao.updateParsed(
            id = noteId,
            status = parseStatus.name,
            nucId = nucId,
            intent = intentResult.intentType.name,
            payloadJson = payloadJson,
            tsEnd = System.currentTimeMillis(),
            parseError = parseError
        )

        return ParseResult(parseStatus, intentResult, nucId, normalized)
    }
}
