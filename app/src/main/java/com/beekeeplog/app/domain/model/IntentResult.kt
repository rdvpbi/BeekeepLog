package com.beekeeplog.app.domain.model

/** NLP extraction result: parsed intent and extracted entities. */
data class IntentResult(
    val intentType: IntentType,
    val entities: Map<String, Any?>
)
