package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.data.room.dao.VoiceNoteDao
import com.beekeeplog.app.data.room.entity.VoiceNoteEntity
import java.util.UUID
import javax.inject.Inject

/**
 * Immediately persists the raw transcript as a [VoiceNoteEntity] with status RAW_SAVED.
 * This happens before NLP parsing so the text is never lost even if parsing fails.
 * Returns the generated note ID for subsequent [ParseVoiceNoteUseCase] call.
 */
class SaveRawVoiceNoteUseCase @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao
) {
    suspend operator fun invoke(rawText: String, tsStart: Long): String {
        val noteId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        voiceNoteDao.insert(
            VoiceNoteEntity(
                id = noteId,
                tsStart = tsStart,
                tsEnd = now,
                rawText = rawText,
                status = "RAW_SAVED",
                createdAt = now
            )
        )
        return noteId
    }
}
