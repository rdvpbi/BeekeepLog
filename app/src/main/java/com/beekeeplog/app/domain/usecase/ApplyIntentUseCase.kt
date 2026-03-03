package com.beekeeplog.app.domain.usecase

import androidx.room.withTransaction
import com.beekeeplog.app.data.room.AppDatabase
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.data.room.entity.QueenEntity
import com.beekeeplog.app.data.room.entity.TaskEntity
import com.beekeeplog.app.domain.model.DbChangeSummary
import com.beekeeplog.app.domain.model.IntentResult
import com.beekeeplog.app.domain.model.IntentType
import java.util.UUID
import javax.inject.Inject

/**
 * UC-08: Applies a confirmed intent to the database within a transaction.
 * Handles all 16 intent types per spec table UC-08.
 * Task due dates per spec: HATCHING=+3d, MATING_FLIGHT=+12d.
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
                // Remove old queen from nuc if present
                queen?.let { db.queenDao().clearNucId(it.id, now) }
                // Create new queen with genetics from intent entities (+3d HATCHING per spec)
                val genetics = (intentResult.entities["genetics"] as? String) ?: "CARNICA"
                val lineName = intentResult.entities["lineName"] as? String
                val newId = UUID.randomUUID().toString()
                db.queenDao().insert(QueenEntity(
                    id = newId,
                    genetics = genetics,
                    lineName = lineName,
                    stage = "CELL",
                    lifecycleStatus = "ACTIVE",
                    nucId = nucId,
                    isElite = false,
                    isReserved = false,
                    aggressionScore = null,
                    stageChangedAt = now,
                    createdAt = now,
                    updatedAt = now
                ))
                db.nucDao().setQueen(nucId, newId)
                updatedNucs++
                db.taskDao().insert(TaskEntity(
                    id = UUID.randomUUID().toString(),
                    nucId = nucId,
                    queenId = newId,
                    taskType = "HATCHING",
                    dueAt = now + 3 * 86_400_000L,
                    isCompleted = false,
                    description = "Выход матки из маточника",
                    createdAt = now
                ))
                createdTasks++
            }

            IntentType.UPDATE_STATUS_VIRGIN -> {
                // Find or create queen; complete existing HATCHING task; +12d MATING_FLIGHT
                val targetId: String
                if (queen != null) {
                    db.queenDao().updateStage(queen.id, "VIRGIN", now)
                    db.queenDao().updateStageChangedAt(queen.id, now)
                    targetId = queen.id
                    updatedQueens++
                } else {
                    targetId = UUID.randomUUID().toString()
                    db.queenDao().insert(QueenEntity(
                        id = targetId,
                        genetics = "CARNICA",
                        lineName = null,
                        stage = "VIRGIN",
                        lifecycleStatus = "ACTIVE",
                        nucId = nucId,
                        isElite = false,
                        isReserved = false,
                        aggressionScore = null,
                        stageChangedAt = now,
                        createdAt = now,
                        updatedAt = now
                    ))
                    db.nucDao().setQueen(nucId, targetId)
                    updatedNucs++
                }
                db.taskDao().completeByTypeForNuc(nucId, "HATCHING", now)
                db.taskDao().insert(TaskEntity(
                    id = UUID.randomUUID().toString(),
                    nucId = nucId,
                    queenId = targetId,
                    taskType = "MATING_FLIGHT",
                    dueAt = now + 12 * 86_400_000L,
                    isCompleted = false,
                    description = "Облёт матки",
                    createdAt = now
                ))
                createdTasks++
            }

            IntentType.UPDATE_STATUS_LAYING -> {
                // Set stage LAYING + complete ALL tasks for this queen
                queen?.let {
                    db.queenDao().updateStage(it.id, "LAYING", now)
                    db.queenDao().updateStageChangedAt(it.id, now)
                    updatedQueens++
                    db.taskDao().completeAllForQueen(it.id, now)
                }
            }

            IntentType.MARK_EMPTY_SWARMED -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "LOST", now)
                    db.queenDao().clearNucId(it.id, now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }

            IntentType.MARK_LOST -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "LOST", now)
                    db.queenDao().clearNucId(it.id, now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }

            IntentType.MARK_SOLD -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "SOLD", now)
                    db.queenDao().clearNucId(it.id, now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }

            IntentType.MARK_CULLED -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "CULLED", now)
                    db.queenDao().clearNucId(it.id, now)
                    updatedQueens++
                }
                db.nucDao().setQueen(nucId, null)
                updatedNucs++
            }

            IntentType.MARK_DRONE_LAYER -> {
                queen?.let {
                    db.queenDao().updateLifecycleStatus(it.id, "CULLED", now)
                    db.queenDao().updateQualityNotes(it.id, "трутовка", now)
                    db.queenDao().clearNucId(it.id, now)
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
                    val existing = it.qualityNotes
                    val updated = if (existing.isNullOrBlank()) note else "$existing; $note"
                    db.queenDao().updateQualityNotes(it.id, updated, now)
                    updatedQueens++
                }
            }

            IntentType.NEEDS_FEEDING -> {
                db.taskDao().insert(TaskEntity(
                    id = UUID.randomUUID().toString(),
                    nucId = nucId,
                    queenId = null,
                    taskType = "FEEDING",
                    dueAt = now + 86_400_000L,
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
                    dueAt = now + 86_400_000L,
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
                // No DB writes for queries, no-op or unknown intents
            }
        }

        // Append DB_APPLIED event to audit log
        db.eventDao().insert(EventEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            eventType = "DB_APPLIED",
            payloadJson = """{"intent":"${intentResult.intentType.name}","nucId":$nucId}""",
            ts = now
        ))

        DbChangeSummary(
            updatedNucs = updatedNucs,
            updatedQueens = updatedQueens,
            createdTasks = createdTasks
        )
    }
}
