package com.beekeeplog.app.speech

/** Events emitted by the speech recognition engine. */
sealed class SpeechEvent {
    /** Partial recognition result (not final). */
    data class Partial(val text: String, val confidence: Float? = null) : SpeechEvent()

    /** Final recognition result for this utterance. */
    data class Final(val text: String, val confidence: Float? = null) : SpeechEvent()

    /** Root-mean-square audio level update. */
    data class Rms(val rmsdB: Float) : SpeechEvent()

    /** Recognition error with an error code and human-readable message. */
    data class Error(val errorCode: Int, val message: String) : SpeechEvent()
}
