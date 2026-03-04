package com.beekeeplog.app.presentation.voice

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
 *   IDLE → [button] → LISTENING (STT runs continuously with auto-restart)
 *   LISTENING → [button or 5-min silence] → CONFIRMING (NLP result shown)
 *   CONFIRMING → [ВЕРНО] → SUCCESS → IDLE
 *   CONFIRMING → [ОТМЕНА] → IDLE
 *
 * STT auto-restart is handled by [SpeechEngine] internally (onResults/onError).
 * The silence timer resets on every Partial or Final speech event.
 */
@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val startSessionUC: StartInspectionSessionUseCase,
    private val stopSessionUC: StopInspectionSessionUseCase,
    private val ingestEvent: IngestSpeechEventUseCase,
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

    companion object {
        /** Auto-stop the session if no speech activity for this duration. */
        private const val SILENCE_TIMEOUT_MS = 5 * 60 * 1000L
    }

    init {
        observeSpeechEvents()
        observeAlerts()
    }

    // -------------------------------------------------------------------------
    // Public actions
    // -------------------------------------------------------------------------

    /** Starts a new recording session. Transitions IDLE → LISTENING. */
    fun startSession() {
        if (_uiState.value.phase != VoicePhase.IDLE) return
        viewModelScope.launch {
            startSessionUC().onSuccess { sessionId ->
                soundFeedback.playStart()
                _uiState.update {
                    it.copy(
                        phase = VoicePhase.LISTENING,
                        sessionId = sessionId,
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
     * Stops listening and runs NLP. Always transitions LISTENING → CONFIRMING,
     * regardless of parse result (PARSED_OK / PARSED_PARTIAL / PARSED_FAILED).
     */
    fun stopSession() {
        val state = _uiState.value
        if (state.phase != VoicePhase.LISTENING) return
        silenceJob?.cancel()
        timerJob?.cancel()
        soundFeedback.playStop()

        viewModelScope.launch {
            val sessionId = state.sessionId ?: return@launch
            // rawBuffer holds all accumulated finals; fall back to current partial
            val rawText = state.rawBuffer.ifBlank { state.streamingText }.trim()
            val (intentResult, nucId) = stopSessionUC(sessionId, rawText)

            val parseStatus = when {
                rawText.isBlank() ->
                    ParseStatus.PARSED_FAILED
                intentResult.intentType == IntentType.UNKNOWN && nucId == null ->
                    ParseStatus.PARSED_FAILED
                nucId == null || intentResult.intentType == IntentType.UNKNOWN ->
                    ParseStatus.PARSED_PARTIAL
                else ->
                    ParseStatus.PARSED_OK
            }

            _uiState.update {
                it.copy(
                    phase = VoicePhase.CONFIRMING,
                    recognizedText = rawText,
                    recognizedIntent = intentResult,
                    currentNucId = nucId ?: it.currentNucId,
                    parseStatus = parseStatus,
                    streamingText = ""
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
            val intentResult = state.recognizedIntent ?: return@launch
            val nucId = state.currentNucId

            _uiState.update { it.copy(phase = VoicePhase.SUCCESS) }

            confirmIntentUseCase(sessionId, rawText, normalized, intentResult, nucId)

            delay(if (state.mode == VoiceMode.QUESTION) 6000L else 3000L)
            resetToIdle()
        }
    }

    /** Discards the detected intent. CONFIRMING → IDLE. */
    fun cancelIntent() {
        val state = _uiState.value
        if (state.phase != VoicePhase.CONFIRMING) return

        viewModelScope.launch {
            val sessionId = state.sessionId ?: return@launch
            cancelIntentUseCase(sessionId, state.recognizedText, state.currentNucId)
            resetToIdle()
        }
    }

    /** Switch RECORD / QUESTION mode — only allowed in IDLE. */
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
                // Any speech activity resets the silence auto-stop timer
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
                            task.dueAt < now                        -> AlertSeverity.HIGH
                            task.dueAt < now + 4 * 3_600_000L      -> AlertSeverity.MEDIUM
                            else                                    -> AlertSeverity.LOW
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
                delay(1000L)
                _uiState.update { it.copy(sessionDurationSec = it.sessionDurationSec + 1) }
            }
        }
    }

    /**
     * Resets the 5-minute silence timer. Called at session start and on each speech event.
     * If no speech arrives within [SILENCE_TIMEOUT_MS], the session is stopped automatically.
     */
    private fun resetSilenceTimer() {
        silenceJob?.cancel()
        silenceJob = viewModelScope.launch {
            delay(SILENCE_TIMEOUT_MS)
            if (_uiState.value.phase == VoicePhase.LISTENING) {
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
                currentNucId = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundFeedback.release()
    }
}
