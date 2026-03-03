package com.beekeeplog.app.domain.usecase

import androidx.room.withTransaction
import com.beekeeplog.app.data.room.AppDatabase
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.data.room.entity.TaskEntity
import com.beekeeplog.app.domain.model.DbChangeSummary
import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.IntentType
import java.util.UUID
import javax.inject.Inject

/**
 * UC-08: Applies a confirmed intent to the database within a transaction.
 * Handles all 16 intent types per spec.
 */
class ApplyIntentUseCase @Inject constructor(
    private val db: AppDatabase
) {
    suspend operator fun invoke(
        intentResult: IntentResult,
        nucId: Int?,
        sessionId: String
    ): DbChangeSummary = db.withTransaction {
        if (nucId == null) return@withTransaction DbChangeSummary(0, 0, 0)

        val now = System.currentTimeMillis()
        var updatedQueens = 0
        var updatedNucs = 0
        var createdTasks = 0

        val queen = db.queenDao().getByNucId(nucId)

        when (intentResult.intentType) {
            IntentType.UPDATE_STATUS_CELL -> {
                queen?.let {
                    db.queenDao().updateStage(it.id, "CELL", now)
                    db.queenDao().updateStageChangedAt(it.id, now)
                    updatedQueens++
                    // Create HATCHING task (+7 days)
                    db.taskDao().insert(TaskEntity(
                        id = UUID.randomUUID().toString(),
                        nucId = nucId,
                        queenId = it.id,
                        taskType = "HATCHING",
                        dueAt = now + 7 * 86_400_000L,
                        isCompleted = false,
                        description = "Выход матки из маточника",
                        createdAt = now
                    ))
                    createdTasks++
                }
            }
            IntentType.UPDATE_STATUS_VIRGIN -> {
                queen?.let {
                    db.queenDao().updateStage(it.id, "VIRGIN", now)
                    db.queenDao().updateStageChangedAt(it.id, now)
                    updatedQueens++
                    // Create MATING_FLIGHT task (+5 days)
                    db.taskDao().insert(TaskEntity(
                        id = UUID.randomUUID().toString(),
                        nucId = nucId,
                        queenId = it.id,
                        taskType = "MATING_FLIGHT",
                        dueAt = now + 5 * 86_400_000L,
                        isCompleted = false,
                        description = "Облёт матки",
                        createdAt = now
                    ))
                    createdTasks++
                }
            }
            IntentType.UPDATE_STATUS_LAYING -> {
                queen?.let {
                    db.queenDao().updateStage(it.id, "LAYING", now)
                    db.queenDao().updateStageChangedAt(it.id, now)
                    updatedQueens++
                    // Create CHECK_EGGS task (+7 days)
                    db.taskDao().insert(TaskEntity(
                        id = UUID.randomUUID().toString(),
                        nucId = nucId,
                        queenId = it.id,
                        taskType = "CHECK_EGGS",
                        dueAt = now + 7 * 86_400_000L,
                        isCompleted = false,
                        description = "Проверка яйцекладки",
                        createdAt = now
                    ))
                    createdTasks++
                }
            }
            IntentType.MARK_EMPTY_SWARMED -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "LOST", now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }
            IntentType.MARK_LOST -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "LOST", now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }
            IntentType.MARK_SOLD -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "SOLD", now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }
            IntentType.MARK_CULLED -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "CULLED", now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }
            IntentType.MARK_DRONE_LAYER -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "CULLED", now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }
            IntentType.MARK_ELITE -> {
                queen?.let {
                    db.queenDao().updateElite(it.id, true, now)
                    updatedQueens++
                }
            }
            IntentType.MARK_RESERVED -> {
                queen?.let {
                    db.queenDao().updateReserved(it.id, true, now)
                    updatedQueens++
                }
            }
            IntentType.SET_AGGRESSION -> {
                val score = (intentResult.entities["aggression"] as? Int) ?: 0
                queen?.let {
                    db.queenDao().updateAggression(it.id, score, now)
                    updatedQueens++
                }
            }
            IntentType.ADD_NOTE -> {
                val note = (intentResult.entities["note"] as? String) ?: ""
                queen?.let {
                    db.queenDao().updateQualityNotes(it.id, note, now)
                    updatedQueens++
                }
            }
            IntentType.NEEDS_FEEDING -> {
                db.taskDao().insert(TaskEntity(
                    id = UUID.randomUUID().toString(),
                    nucId = nucId,
                    queenId = null,
                    taskType = "FEEDING",
                    dueAt = now + 1 * 86_400_000L,
                    isCompleted = false,
                    description = "Кормление нуклеуса $nucId",
                    createdAt = now
                ))
                createdTasks++
            }
            IntentType.NEEDS_TREATMENT -> {
                db.taskDao().insert(TaskEntity(
                    id = UUID.randomUUID().toString(),
                    nucId = nucId,
                    queenId = null,
                    taskType = "TREATMENT",
                    dueAt = now + 1 * 86_400_000L,
                    isCompleted = false,
                    description = "Обработка нуклеуса $nucId",
                    createdAt = now
                ))
                createdTasks++
            }
            IntentType.NO_CHANGES,
            IntentType.QUERY_COUNT_BY_STATUS,
            IntentType.QUERY_COUNT_BY_GENETICS,
            IntentType.QUERY_READY_FOR_SALE,
            IntentType.QUERY_EMPTY_NUCS,
            IntentType.QUERY_OVERDUE,
            IntentType.UNKNOWN -> {
                // No DB writes for queries or unknown intents
            }
        }

        // Append DB_APPLIED event
        db.eventDao().insert(
            EventEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                eventType = "DB_APPLIED",
                payloadJson = """{"intent":"${intentResult.intentType.name}","nucId":$nucId}""",
                ts = now
            )
        )

        DbChangeSummary(
            updatedNucs = updatedNucs,
            updatedQueens = updatedQueens,
            createdTasks = createdTasks
        )
    }
}
