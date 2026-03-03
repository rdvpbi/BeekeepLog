package com.beekeeplog.app.presentation.voice

import com.beekeeplog.app.domain.model.AlertItem
import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.VoicePhase
import com.beekeeplog.app.domain.model.VoiceMode

/** Immutable UI state for the Voice screen. */
data class VoiceUiState(
    val phase: VoicePhase = VoicePhase.IDLE,
    val mode: VoiceMode = VoiceMode.RECORD,
    val currentNucId: Int? = null,
    val streamingText: String = "",
    val recognizedText: String = "",
    val recognizedIntent: IntentResult? = null,
    val recognizedEntities: Map<String, Any?> = emptyMap(),
    val rmsValues: List<Float> = List(12) { 0f },
    val confidence: Float = 0f,
    val sessionDurationSec: Int = 0,
    val answerText: String = "",
    val errorText: String = "",
    val todayAlerts: List<AlertItem> = emptyList(),
    val sessionId: String? = null
)
