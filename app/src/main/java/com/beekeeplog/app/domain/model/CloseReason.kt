package com.beekeeplog.app.domain.model

/** Reason why an inspection session segment was closed. */
enum class CloseReason {
    HIVE_SWITCH,
    PAUSE,
    STOP
}
