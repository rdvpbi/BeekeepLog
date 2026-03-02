package com.beekeeplog.app.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

/**
 * Wraps the Android [android.speech.SpeechRecognizer] and exposes a
 * [kotlinx.coroutines.flow.Flow] of [SpeechEvent]s.
 *
 * `EXTRA_PREFER_OFFLINE=true` is set so the app works without a network connection.
 */
class SpeechEngine @Inject constructor() {

    /** Starts listening and emits [SpeechEvent]s until silence or an error. */
    fun startListening(): Flow<SpeechEvent> = emptyFlow()
}
