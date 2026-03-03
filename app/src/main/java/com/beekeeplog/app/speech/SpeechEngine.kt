package com.beekeeplog.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the Android [SpeechRecognizer] and exposes a [SharedFlow] of [SpeechEvent]s.
 * EXTRA_PREFER_OFFLINE=true ensures fully offline operation per spec.
 * All SpeechRecognizer calls happen on the Main thread as required by the API.
 */
@Singleton
class SpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _events = MutableSharedFlow<SpeechEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<SpeechEvent> = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var recognizer: SpeechRecognizer? = null
    @Volatile private var isListening: Boolean = false
    private var consecutiveErrors: Int = 0

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { consecutiveErrors = 0 }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {
            _events.tryEmit(SpeechEvent.Rms(rmsdB))
        }
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            consecutiveErrors++
            val msg = errorMessage(error)
            _events.tryEmit(SpeechEvent.Error(error, msg))
            if (consecutiveErrors >= 3) {
                isListening = false
                consecutiveErrors = 0
            } else if (isListening) {
                scope.launch { startRecognition() }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
            val text = matches?.firstOrNull() ?: ""
            val conf = confidences?.firstOrNull()
            if (text.isNotBlank()) {
                _events.tryEmit(SpeechEvent.Final(text, conf))
            }
            if (isListening) {
                scope.launch { startRecognition() }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                _events.tryEmit(SpeechEvent.Partial(text))
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    /** Starts continuous listening. [languageTag] defaults to "ru-RU". */
    fun start(languageTag: String = "ru-RU", preferOffline: Boolean = true) {
        scope.launch {
            isListening = true
            consecutiveErrors = 0
            ensureRecognizer()
            startRecognition(languageTag, preferOffline)
        }
    }

    /** Stops listening and clears state. */
    fun stop() {
        isListening = false
        scope.launch {
            recognizer?.stopListening()
        }
    }

    /** Releases all resources. */
    fun destroy() {
        isListening = false
        scope.launch {
            recognizer?.destroy()
            recognizer = null
        }
    }

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
            if (preferOffline) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer?.startListening(intent)
    }

    private fun errorMessage(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO              -> "Ошибка аудио"
        SpeechRecognizer.ERROR_CLIENT             -> "Ошибка клиента"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Нет разрешения на запись"
        SpeechRecognizer.ERROR_NETWORK            -> "Ошибка сети"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT    -> "Таймаут сети"
        SpeechRecognizer.ERROR_NO_MATCH           -> "Речь не распознана"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY    -> "Распознаватель занят"
        SpeechRecognizer.ERROR_SERVER             -> "Ошибка сервера"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT     -> "Таймаут речи"
        else                                       -> "Неизвестная ошибка ($code)"
    }
}
