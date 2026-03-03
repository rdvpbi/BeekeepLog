package com.beekeeplog.app.speech

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays short audio feedback tones using [ToneGenerator].
 * Must call [release] when no longer needed.
 */
@Singleton
class SoundFeedback @Inject constructor() {

    private val toneGen: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
    }.getOrNull()

    /** Plays a start-listening beep. */
    fun playStart() {
        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    /** Plays a stop-listening beep. */
    fun playStop() {
        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
    }

    /** Plays a confirmation acknowledgement tone. */
    fun playConfirm() {
        toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
    }

    /** Releases the [ToneGenerator] resources. */
    fun release() {
        toneGen?.release()
    }
}
