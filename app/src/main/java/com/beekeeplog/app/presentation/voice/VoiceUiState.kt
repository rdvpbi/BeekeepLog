package com.beekeeplog.app.presentation.voice

import com.beekeeplog.app.domain.model.AlertItem
import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.ParseStatus
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.domain.model.VoicePhase

/** Immutable UI state for the Voice screen. */
data class VoiceUiState(
    val phase: VoicePhase = VoicePhase.IDLE,
    val mode: VoiceMode = VoiceMode.RECORD,
    val currentNucId: Int? = null,

    /** Live partial text from the current STT recognition window. */
    val streamingText: String = "",

    /** Accumulated finals from all completed STT windows in this session. */
    val rawBuffer: String = "",

    /** Full text shown in the confirmation card (= rawBuffer at moment of stop). */
    val recognizedText: String = "",

    val recognizedIntent: IntentResult? = null,
    val parseStatus: ParseStatus = ParseStatus.NONE,

    val rmsValues: List<Float> = List(12) { 0f },
    val confidence: Float = 0f,
    val sessionDurationSec: Int = 0,
    val answerText: String = "",
    val errorText: String = "",
    val todayAlerts: List<AlertItem> = emptyList(),
    val sessionId: String? = null
)
