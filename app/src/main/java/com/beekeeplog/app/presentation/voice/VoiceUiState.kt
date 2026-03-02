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
    val partialText: String = "",
    val pendingIntent: IntentResult? = null,
    val sessionId: String? = null,
    val alerts: List<AlertItem> = emptyList(),
    val rmsLevel: Float = 0f,
    val errorMessage: String? = null
)
