package com.beekeeplog.app.domain.model

/** Current phase of the voice recording UI. */
enum class VoicePhase {
    IDLE,
    LISTENING,
    CONFIRMING,
    SUCCESS
}
