package com.beekeeplog.app.domain.model

/** 16 intent types recognised by the NLP pipeline. */
enum class IntentType {
    UPDATE_STATUS_LAYING,
    UPDATE_STATUS_VIRGIN,
    UPDATE_STATUS_CELL,
    UPDATE_STATUS_SOLD,
    UPDATE_STATUS_LOST,
    UPDATE_STATUS_CULLED,
    MARK_ELITE,
    MARK_RESERVED,
    MARK_AGGRESSIVE,
    SET_AGGRESSION,
    CONFIRM_MATING,
    CONFIRM_HATCHING,
    CONFIRM_EGGS,
    FEEDING_DONE,
    TREATMENT_DONE,
    UNKNOWN
}
