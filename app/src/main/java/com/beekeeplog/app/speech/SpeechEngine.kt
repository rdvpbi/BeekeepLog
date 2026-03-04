package com.beekeeplog.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the Android [SpeechRecognizer] for continuous long-form recording.
 *
 * Design:
 *   - Android STT works in short bursts (~5–15 s). This engine auto-restarts
 *     after each burst so the user-visible session remains open indefinitely.
 *   - [TranscriptAccumulator] stitches individual results into one transcript.
 *   - Silence extras ask Android to wait longer before ending a burst:
 *       COMPLETE_SILENCE = 2 s, POSSIBLY_COMPLETE = 1.2 s, MINIMUM_LENGTH = 15 s.
 *   - Soft errors (NO_MATCH, SPEECH_TIMEOUT) trigger exponential backoff restart.
 *   - RECOGNIZER_BUSY → destroy + recreate after 600 ms.
 *   - Hard errors: up to 5 consecutive before giving up.
 *   - onEndOfSpeech (normal pause) → restart after 200 ms.
 *   - All SpeechRecognizer calls happen on the Main thread (platform requirement).
 */
@Singleton
class SpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _events = MutableSharedFlow<SpeechEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<SpeechEvent> = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var recognizer: SpeechRecognizer? = null
    @Volatile private var isListening: Boolean = false
    private var consecutiveErrors: Int = 0

    /** Backoff delays (ms) for soft errors: NO_MATCH / SPEECH_TIMEOUT. */
    private val backoffMs = longArrayOf(300L, 800L, 1_500L)
    private var backoffIdx = 0

    /** Accumulates finals + partials across multiple STT windows. */
    val accumulator = TranscriptAccumulator()

    // -------------------------------------------------------------------------
    // RecognitionListener
    // -------------------------------------------------------------------------

    private val listener = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "[STT] onReadyForSpeech  ts=${ts()}")
            consecutiveErrors = 0
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "[STT] onBeginningOfSpeech ts=${ts()}")
        }

        override fun onRmsChanged(rmsdB: Float) {
            _events.tryEmit(SpeechEvent.Rms(rmsdB))
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        /**
         * Called when Android detects end of speech (natural pause / silence).
         * We treat this as an internal window end, NOT as end of user session:
         * restart recognition after 200 ms if still in recording mode.
         */
        override fun onEndOfSpeech() {
            Log.d(TAG, "[STT] onEndOfSpeech      ts=${ts()} isListening=$isListening")
            if (isListening) {
                scope.launch {
                    delay(200L)
                    if (isListening) startRecognition()
                }
            }
        }

        override fun onError(error: Int) {
            val msg = errorMessage(error)
            Log.w(TAG, "[STT] onError code=$error ($msg) backoffIdx=$backoffIdx consecutive=$consecutiveErrors ts=${ts()}")
            _events.tryEmit(SpeechEvent.Error(error, msg))
            if (!isListening) return

            when (error) {
                // Soft errors — speech not detected in window; restart with backoff
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    val delay = backoffMs.getOrElse(backoffIdx) { 1_500L }
                    backoffIdx = (backoffIdx + 1).coerceAtMost(backoffMs.size - 1)
                    Log.d(TAG, "[STT] soft error → restart in ${delay} ms")
                    scope.launch { delay(delay); if (isListening) startRecognition() }
                }
                // Recognizer busy — destroy and recreate
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                    Log.w(TAG, "[STT] recognizer busy → recreate in 600 ms")
                    scope.launch {
                        delay(600L)
                        recognizer?.destroy()
                        recognizer = null
                        if (isListening) { ensureRecognizer(); startRecognition() }
                    }
                }
                // Hard errors — count; give up after 5
                else -> {
                    consecutiveErrors++
                    if (consecutiveErrors >= 5) {
                        Log.e(TAG, "[STT] 5 consecutive hard errors — stopping engine")
                        isListening = false
                        consecutiveErrors = 0
                    } else {
                        scope.launch { delay(500L); if (isListening) startRecognition() }
                    }
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
            val rawText = matches?.firstOrNull() ?: ""
            val conf = confidences?.firstOrNull()

            val effective = accumulator.onFinal(rawText)
            Log.d(TAG, "[STT] onResults  raw=\"$rawText\"  effective=\"$effective\"  conf=$conf  ts=${ts()}")

            if (effective.isNotBlank()) {
                backoffIdx = 0          // successful result resets backoff
                _events.tryEmit(SpeechEvent.Final(effective, conf))
            }

            if (isListening) {
                scope.launch { startRecognition() }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                Log.v(TAG, "[STT] partial: \"$text\"")
                accumulator.onPartial(text)
                backoffIdx = 0          // any speech activity resets backoff
                _events.tryEmit(SpeechEvent.Partial(text))
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Starts continuous listening. Resets the [accumulator] for a fresh session.
     * [languageTag] defaults to "ru-RU"; [preferOffline] = true per spec.
     */
    fun start(languageTag: String = "ru-RU", preferOffline: Boolean = true) {
        scope.launch {
            isListening = true
            consecutiveErrors = 0
            backoffIdx = 0
            accumulator.reset()
            Log.i(TAG, "[STT] start() — new session  ts=${ts()}")
            ensureRecognizer()
            startRecognition(languageTag, preferOffline)
        }
    }

    /**
     * Stops listening. Call [compile] BEFORE this to get the final transcript,
     * as the accumulator is not cleared here (call [resetAccumulator] when done).
     */
    fun stop() {
        Log.i(TAG, "[STT] stop()  ts=${ts()}")
        isListening = false
        scope.launch { recognizer?.stopListening() }
    }

    /**
     * Returns the compiled full transcript of everything heard in this session.
     * Includes a fresh pending partial if present.
     */
    fun compile(): String = accumulator.compile()

    /** Releases all resources. */
    fun destroy() {
        isListening = false
        scope.launch {
            recognizer?.destroy()
            recognizer = null
        }
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private fun ensureRecognizer() {
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(listener)
            }
        }
    }

    private fun startRecognition(languageTag: String = "ru-RU", preferOffline: Boolean = true) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            if (preferOffline) putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

            // Extended silence windows — reduces premature session termination.
            // These extras are semi-public; not all ROMs honour them, but they
            // improve behaviour on Google STT and many OEM implementations.
            putExtra(EXTRA_COMPLETE_SILENCE_MS,          2_000L)
            putExtra(EXTRA_POSSIBLY_COMPLETE_SILENCE_MS, 1_200L)
            putExtra(EXTRA_MINIMUM_LENGTH_MS,           15_000L)
        }
        Log.d(TAG, "[STT] startListening  ts=${ts()}")
        recognizer?.startListening(intent)
    }

    private fun errorMessage(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO                -> "Ошибка аудио"
        SpeechRecognizer.ERROR_CLIENT               -> "Ошибка клиента"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Нет разрешения"
        SpeechRecognizer.ERROR_NETWORK              -> "Ошибка сети"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT      -> "Таймаут сети"
        SpeechRecognizer.ERROR_NO_MATCH             -> "Нет совпадения"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY      -> "Распознаватель занят"
        SpeechRecognizer.ERROR_SERVER               -> "Ошибка сервера"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT       -> "Таймаут речи"
        else                                         -> "Ошибка $code"
    }

    private fun ts() = System.currentTimeMillis()

    companion object {
        private const val TAG = "SpeechEngine"

        // Semi-public Android STT extras for silence timing (exist since API 8)
        private const val EXTRA_COMPLETE_SILENCE_MS =
            "android.speech.extra.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS"
        private const val EXTRA_POSSIBLY_COMPLETE_SILENCE_MS =
            "android.speech.extra.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS"
        private const val EXTRA_MINIMUM_LENGTH_MS =
            "android.speech.extra.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS"
    }
}
