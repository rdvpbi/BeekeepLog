package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.presentation.voice.VoiceUiState
import com.beekeeplog.app.segmenter.HiveDetector
import com.beekeeplog.app.speech.SpeechEvent
import javax.inject.Inject

/**
 * UC-03: Processes a single [SpeechEvent] and returns an updated [VoiceUiState].
 * Partial/Final events update streaming text and detected hive.
 * Rms events update the RMS bar values.
 * Error events set the error text.
 */
class IngestSpeechEventUseCase @Inject constructor(
    private val hiveDetector: HiveDetector
) {
    operator fun invoke(event: SpeechEvent, current: VoiceUiState): VoiceUiState = when (event) {
        is SpeechEvent.Partial -> {
            val nucId = hiveDetector.detect(event.text)
            current.copy(
                streamingText = event.text,
                currentNucId = nucId ?: current.currentNucId,
                confidence = event.confidence ?: current.confidence
            )
        }
        is SpeechEvent.Final -> {
            val nucId = hiveDetector.detect(event.text)
            current.copy(
                streamingText = "",
                recognizedText = event.text,
                currentNucId = nucId ?: current.currentNucId,
                confidence = event.confidence ?: current.confidence
            )
        }
        is SpeechEvent.Rms -> {
            val normalized = ((event.rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            val newBars = current.rmsValues.toMutableList().also { bars ->
                bars.removeAt(0)
                bars.add(normalized)
            }
            current.copy(rmsValues = newBars)
        }
        is SpeechEvent.Error -> {
            current.copy(errorText = event.message)
        }
    }
}
