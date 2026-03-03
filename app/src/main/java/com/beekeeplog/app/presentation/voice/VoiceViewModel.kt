package com.beekeeplog.app.presentation.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beekeeplog.app.data.room.dao.TaskDao
import com.beekeeplog.app.domain.model.AlertItem
import com.beekeeplog.app.domain.model.AlertSeverity
import com.beekeeplog.app.domain.model.VoiceMode
import com.beekeeplog.app.domain.model.VoicePhase
import com.beekeeplog.app.domain.usecase.CancelIntentUseCase
import com.beekeeplog.app.domain.usecase.ConfirmIntentUseCase
import com.beekeeplog.app.domain.usecase.IngestSpeechEventUseCase
import com.beekeeplog.app.domain.usecase.StartInspectionSessionUseCase
import com.beekeeplog.app.domain.usecase.StopInspectionSessionUseCase
import com.beekeeplog.app.nlp.IntentExtractor
import com.beekeeplog.app.nlp.Normalizer
import com.beekeeplog.app.speech.SoundFeedback
import com.beekeeplog.app.speech.SpeechEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel for the Voice screen. Delegates all business logic to Use Cases. */
@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val startSession: StartInspectionSessionUseCase,
    private val stopSession: StopInspectionSessionUseCase,
    private val ingestEvent: IngestSpeechEventUseCase,
    private val confirmIntentUseCase: ConfirmIntentUseCase,
    private val cancelIntentUseCase: CancelIntentUseCase,
    private val speechEngine: SpeechEngine,
    private val soundFeedback: SoundFeedback,
    private val normalizer: Normalizer,
    private val intentExtractor: IntentExtractor,
    private val taskDao: TaskDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var autoStopJob: Job? = null
    private var timerJob: Job? = null

    init {
        observeSpeechEvents()
        observeAlerts()
    }

    // -------------------------------------------------------------------------
    // Public actions
    // -------------------------------------------------------------------------

    /** Starts a new recording session after permission check. */
    fun startSession() {
        if (_uiState.value.phase != VoicePhase.IDLE) return
        viewModelScope.launch {
            val result = startSession.invoke()
            result.onSuccess { sessionId ->
                soundFeedback.playStart()
                _uiState.update { it.copy(
                    phase = VoicePhase.LISTENING,
                    sessionId = sessionId,
                    streamingText = "",
                    recognizedText = "",
                    recognizedIntent = null,
                    errorText = "",
                    sessionDurationSec = 0
                ) }
                startTimer()
                scheduleAutoStop()
            }.onFailure { err ->
                _uiState.update { it.copy(errorText = err.message ?: "Ошибка запуска") }
            }
        }
    }

    /** Stops listening, runs NLP, transitions to CONFIRMING if intent found, else IDLE. */
    fun stopSession() {
        val state = _uiState.value
        if (state.phase != VoicePhase.LISTENING) return
        autoStopJob?.cancel()
        timerJob?.cancel()
        soundFeedback.playStop()

        viewModelScope.launch {
            val sessionId = state.sessionId ?: return@launch
            val rawText = if (state.recognizedText.isNotBlank()) state.recognizedText else state.streamingText
            val (intentResult, nucId) = stopSession.invoke(sessionId, rawText)

            if (intentResult.intentType.name == "UNKNOWN" || rawText.isBlank()) {
                _uiState.update { it.copy(phase = VoicePhase.IDLE, streamingText = "", sessionId = null) }
            } else {
                _uiState.update { it.copy(
                    phase = VoicePhase.CONFIRMING,
                    recognizedIntent = intentResult,
                    currentNucId = nucId ?: it.currentNucId,
                    streamingText = ""
                ) }
            }
        }
    }

    /** User said "ВЕРНО" — apply intent to DB. */
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
            _uiState.update { it.copy(
                phase = VoicePhase.IDLE,
                sessionId = null,
                recognizedIntent = null,
                recognizedText = "",
                currentNucId = null
            ) }
        }
    }

    /** User said "ОТМЕНА" — discard intent. */
    fun cancelIntent() {
        val state = _uiState.value
        if (state.phase != VoicePhase.CONFIRMING) return

        viewModelScope.launch {
            val sessionId = state.sessionId ?: return@launch
            cancelIntentUseCase(sessionId, state.recognizedText, state.currentNucId)
            _uiState.update { it.copy(
                phase = VoicePhase.IDLE,
                sessionId = null,
                recognizedIntent = null,
                recognizedText = "",
                currentNucId = null
            ) }
        }
    }

    /** Switch RECORD/QUESTION mode — only allowed in IDLE. */
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
                            task.dueAt < now -> AlertSeverity.HIGH
                            task.dueAt < now + 4 * 3600_000L -> AlertSeverity.MEDIUM
                            else -> AlertSeverity.LOW
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

    private fun scheduleAutoStop() {
        autoStopJob?.cancel()
        autoStopJob = viewModelScope.launch {
            delay(4000L)
            if (_uiState.value.phase == VoicePhase.LISTENING) {
                stopSession()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundFeedback.release()
    }
}
