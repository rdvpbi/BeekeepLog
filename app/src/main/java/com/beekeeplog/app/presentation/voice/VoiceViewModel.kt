package com.beekeeplog.app.presentation.voice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeplog.app.data.room.dao.TaskDao
import com.beekeeplog.app.domain.model.AlertItem
import com.beekeeplog.app.domain.model.AlertSeverity
import com.beekeeplog.app.domain.model.IntentType
import com.beekeeplog.app.domain.model.ParseStatus
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.domain.model.VoicePhase
import com.beekeeplog.app.domain.usecase.CancelIntentUseCase
import com.beekeeplog.app.domain.usecase.ConfirmIntentUseCase
import com.beekeeplog.app.domain.usecase.IngestSpeechEventUseCase
import com.beekeeplog.app.domain.usecase.ParseVoiceNoteUseCase
import com.beekeeplog.app.domain.usecase.SaveRawVoiceNoteUseCase
import com.beekeeplog.app.domain.usecase.StartInspectionSessionUseCase
import com.beekeeplog.app.domain.usecase.StopInspectionSessionUseCase
import com.beekeeplog.app.nlp.Normalizer
import com.beekeeplog.app.speech.SoundFeedback
import com.beekeeplog.app.speech.SpeechEngine
import com.beekeeplog.app.speech.SpeechEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Voice screen.
 *
 * Session lifecycle:
 *   IDLE → [button] → LISTENING  (continuous STT, silence timer armed)
 *   LISTENING → [button | 5-min silence] → CONFIRMING
 *     Sub-steps in CONFIRMING:
 *       1. ParseStatus.NONE   — RAW saved, NLP running (loading indicator)
 *       2. ParseStatus.PARSED_* — NLP done, card shown to user
 *   CONFIRMING → [ВЕРНО] → SUCCESS (3 s) → IDLE
 *   CONFIRMING → [ОТМЕНА] → IDLE
 *
 * Anti-garbage rules (spec 3.1):
 *   - rawText.length < 3 → silently discard, back to IDLE
 *   - rawText in [GARBAGE_PHRASES] → discard
 *   - Duplicate of last saved text within 10 s → discard
 */
@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val startSessionUC: StartInspectionSessionUseCase,
    private val stopSessionUC: StopInspectionSessionUseCase,
    private val ingestEvent: IngestSpeechEventUseCase,
    private val saveRawNote: SaveRawVoiceNoteUseCase,
    private val parseNote: ParseVoiceNoteUseCase,
    private val confirmIntentUseCase: ConfirmIntentUseCase,
    private val cancelIntentUseCase: CancelIntentUseCase,
    private val speechEngine: SpeechEngine,
    private val soundFeedback: SoundFeedback,
    private val normalizer: Normalizer,
    private val taskDao: TaskDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var silenceJob: Job? = null
    private var timerJob: Job? = null

    // Dedup state: don't save identical text twice within 10 s
    private var lastSavedRaw: String = ""
    private var lastSavedTs: Long = 0L

    // lastKnownNucId fallback: if user mentioned a hive, remember it briefly
    private var lastKnownNucId: Int? = null
    private var lastNucTs: Long = 0L

    companion object {
        private const val TAG = "VoiceViewModel"
        private const val SILENCE_TIMEOUT_MS = 5 * 60 * 1_000L
        private const val NUC_TTL_MS = 40_000L
        private const val DEDUP_WINDOW_MS = 10_000L

        private val GARBAGE_PHRASES = setOf(
            "да", "нет", "угу", "ну", "вот", "ага", "о", "а", "э", "м", "ну вот", "ну да"
        )
    }

    init {
        observeSpeechEvents()
        observeAlerts()
    }

    // -------------------------------------------------------------------------
    // Public actions
    // -------------------------------------------------------------------------

    /** Starts a new recording session. IDLE → LISTENING. */
    fun startSession() {
        if (_uiState.value.phase != VoicePhase.IDLE) return
        viewModelScope.launch {
            startSessionUC().onSuccess { sessionId ->
                val now = System.currentTimeMillis()
                soundFeedback.playStart()
                _uiState.update {
                    it.copy(
                        phase = VoicePhase.LISTENING,
                        sessionId = sessionId,
                        sessionStartMs = now,
                        streamingText = "",
                        rawBuffer = "",
                        recognizedText = "",
                        recognizedIntent = null,
                        parseStatus = ParseStatus.NONE,
                        errorText = "",
                        sessionDurationSec = 0,
                        currentNucId = null
                    )
                }
                startTimer()
                resetSilenceTimer()
            }.onFailure { err ->
                _uiState.update { it.copy(errorText = err.message ?: "Ошибка запуска") }
            }
        }
    }

    /**
     * Stops listening and starts the save + parse pipeline.
     * LISTENING → CONFIRMING (ParseStatus.NONE while parsing, then PARSED_*)
     */
    fun stopSession() {
        val state = _uiState.value
        if (state.phase != VoicePhase.LISTENING) return
        silenceJob?.cancel()
        timerJob?.cancel()
        soundFeedback.playStop()

        // Compile transcript BEFORE stopping STT (accumulator includes fresh partial)
        val rawText = speechEngine.compile()
        Log.i(TAG, "stopSession() compiled rawText=\"$rawText\"")

        viewModelScope.launch {
            val sessionId = state.sessionId ?: return@launch

            // Stop STT and update DB session
            stopSessionUC(sessionId)

            // Anti-garbage filter
            if (!isValidRaw(rawText)) {
                Log.i(TAG, "stopSession() discarded: raw=\"$rawText\"")
                _uiState.update {
                    it.copy(
                        phase = VoicePhase.IDLE,
                        streamingText = "",
                        rawBuffer = "",
                        sessionId = null,
                        errorText = if (rawText.isBlank()) "Ничего не услышал" else ""
                    )
                }
                return@launch
            }

            // 1. Immediately save RAW — text is preserved no matter what happens next
            val noteId = saveRawNote(rawText, state.sessionStartMs)
            lastSavedRaw = rawText
            lastSavedTs = System.currentTimeMillis()
            Log.i(TAG, "stopSession() RAW saved noteId=$noteId")

            // 2. Show loading state while NLP runs
            _uiState.update {
                it.copy(
                    phase = VoicePhase.CONFIRMING,
                    recognizedText = rawText,
                    parseStatus = ParseStatus.NONE,
                    streamingText = ""
                )
            }

            // 3. Run NLP (may take a few hundred ms on large Dictionary)
            val lastNuc = if (System.currentTimeMillis() - lastNucTs <= NUC_TTL_MS) lastKnownNucId else null
            val result = parseNote(noteId, rawText, lastNuc)
            Log.i(TAG, "stopSession() parse done: status=${result.parseStatus} nuc=${result.nucId} intent=${result.intentResult.intentType}")

            // Remember detected nuc for next utterance
            if (result.nucId != null) {
                lastKnownNucId = result.nucId
                lastNucTs = System.currentTimeMillis()
            }

            // 4. Update UI with parse result
            _uiState.update {
                it.copy(
                    parseStatus = result.parseStatus,
                    recognizedIntent = result.intentResult,
                    currentNucId = result.nucId ?: it.currentNucId
                )
            }
        }
    }

    /** Confirms the detected intent and writes it to the DB. CONFIRMING → SUCCESS → IDLE. */
    fun confirmIntent() {
        val state = _uiState.value
        if (state.phase != VoicePhase.CONFIRMING) return
        soundFeedback.playConfirm()

        viewModelScope.launch {
            val sessionId = state.sessionId ?: return@launch
            val rawText = state.recognizedText
            val normalized = normalizer.normalize(rawText)
            val intentResult = state.recognizedIntent
                ?: return@launch   // shouldn't happen; PARSED_FAILED still has an UNKNOWN result

            _uiState.update { it.copy(phase = VoicePhase.SUCCESS) }

            confirmIntentUseCase(sessionId, rawText, normalized, intentResult, state.currentNucId)

            delay(if (state.mode == VoiceMode.QUESTION) 6_000L else 3_000L)
            resetToIdle()
        }
    }

    /** Discards the pending intent. CONFIRMING → IDLE. */
    fun cancelIntent() {
        val state = _uiState.value
        if (state.phase != VoicePhase.CONFIRMING) return

        viewModelScope.launch {
            val sessionId = state.sessionId ?: return@launch
            cancelIntentUseCase(sessionId, state.recognizedText, state.currentNucId)
            resetToIdle()
        }
    }

    /** Switch RECORD / QUESTION mode — only in IDLE. */
    fun onModeChange(mode: VoiceMode) {
        if (_uiState.value.phase != VoicePhase.IDLE) return
        _uiState.update { it.copy(mode = mode) }
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private fun observeSpeechEvents() {
        viewModelScope.launch {
            speechEngine.events.collect { event ->
                if (_uiState.value.phase == VoicePhase.LISTENING &&
                    (event is SpeechEvent.Partial || event is SpeechEvent.Final)
                ) {
                    resetSilenceTimer()
                }
                _uiState.update { current -> ingestEvent(event, current) }
            }
        }
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            taskDao.getAllFlow().collect { tasks ->
                val now = System.currentTimeMillis()
                val todayEnd = now + 86_400_000L
                val alerts = tasks
                    .filter { !it.isCompleted && it.dueAt <= todayEnd }
                    .map { task ->
                        val severity = when {
                            task.dueAt < now                   -> AlertSeverity.HIGH
                            task.dueAt < now + 4 * 3_600_000L -> AlertSeverity.MEDIUM
                            else                               -> AlertSeverity.LOW
                        }
                        AlertItem(
                            nucId = task.nucId,
                            message = task.description ?: "${task.taskType} нуклеус ${task.nucId}",
                            severity = severity
                        )
                    }
                    .sortedBy { it.severity.ordinal }
                _uiState.update { it.copy(todayAlerts = alerts) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _uiState.update { it.copy(sessionDurationSec = it.sessionDurationSec + 1) }
            }
        }
    }

    private fun resetSilenceTimer() {
        silenceJob?.cancel()
        silenceJob = viewModelScope.launch {
            delay(SILENCE_TIMEOUT_MS)
            if (_uiState.value.phase == VoicePhase.LISTENING) {
                Log.i(TAG, "5-min silence auto-stop triggered")
                stopSession()
            }
        }
    }

    private fun resetToIdle() {
        _uiState.update {
            it.copy(
                phase = VoicePhase.IDLE,
                sessionId = null,
                recognizedIntent = null,
                recognizedText = "",
                rawBuffer = "",
                parseStatus = ParseStatus.NONE,
                currentNucId = null,
                errorText = ""
            )
        }
    }

    /**
     * Spec 3.1 anti-garbage filter.
     * Returns false if text is too short, known garbage phrase, or a duplicate of the last save.
     */
    private fun isValidRaw(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.length < 3) return false
        if (trimmed.lowercase() in GARBAGE_PHRASES) return false
        if (trimmed == lastSavedRaw && System.currentTimeMillis() - lastSavedTs < DEDUP_WINDOW_MS) {
            Log.w(TAG, "duplicate raw text discarded: \"$trimmed\"")
            return false
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        soundFeedback.release()
    }
}
