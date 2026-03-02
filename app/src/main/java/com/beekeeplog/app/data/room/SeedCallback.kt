package com.beekeeplog.app.data.room

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.beekeeplog.app.data.room.entity.NucEntity
import com.beekeeplog.app.data.room.entity.QueenEntity
import com.beekeeplog.app.data.room.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

/**
 * Pre-populates the database with 50 nucs, 35 queens and 20 tasks on first creation.
 * Injected via Hilt and registered with [RoomDatabase.Builder.addCallback].
 */
class SeedCallback @Inject constructor(
    private val databaseProvider: Provider<AppDatabase>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val database = databaseProvider.get()
            seedNucs(database)
            seedQueens(database)
            seedTasks(database)
        }
    }

    // -------------------------------------------------------------------------
    // Nucs — 50 nuclei across 5 rows (A row1/row2, B row1/row2, C row1)
    // IDs 16-20, 25-30, 36-40, 47-50 remain empty (no queen assigned)
    // -------------------------------------------------------------------------
    private suspend fun seedNucs(db: AppDatabase) {
        val nucs = buildList {
            // A-row1: ids 1–10
            for (i in 1..10) add(NucEntity(id = i, sector = "A", row = 1, position = i, currentQueenId = null))
            // A-row2: ids 11–20
            for (i in 11..20) add(NucEntity(id = i, sector = "A", row = 2, position = i - 10, currentQueenId = null))
            // B-row1: ids 21–30
            for (i in 21..30) add(NucEntity(id = i, sector = "B", row = 1, position = i - 20, currentQueenId = null))
            // B-row2: ids 31–40
            for (i in 31..40) add(NucEntity(id = i, sector = "B", row = 2, position = i - 30, currentQueenId = null))
            // C-row1: ids 41–50
            for (i in 41..50) add(NucEntity(id = i, sector = "C", row = 1, position = i - 40, currentQueenId = null))
        }
        db.nucDao().insertAll(nucs)
    }

    // -------------------------------------------------------------------------
    // Queens: Q01–Q12 LAYING, Q13–Q22 VIRGIN, Q23–Q30 CELL, Q31–Q33 SOLD, Q34–Q35 LOST
    // -------------------------------------------------------------------------
    private suspend fun seedQueens(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        val queens = listOf(
            // --- LAYING (Q01–Q12) assigned to nucs 1–12 / 21-24 ---
            queen("Q01", "CARNICA",   "Альфа",    "LAYING", "ACTIVE", 1,  now - 30 * day, now),
            queen("Q02", "BUCKFAST",  "Бакфаст-1","LAYING", "ACTIVE", 2,  now - 28 * day, now),
            queen("Q03", "MELLIFERA", null,        "LAYING", "ACTIVE", 3,  now - 25 * day, now),
            queen("Q04", "CARNICA",   "Скленар",  "LAYING", "ACTIVE", 4,  now - 22 * day, now),
            queen("Q05", "CARPATHICA",null,        "LAYING", "ACTIVE", 5,  now - 20 * day, now),
            queen("Q06", "ITALIANA",  "Кордован", "LAYING", "ACTIVE", 6,  now - 18 * day, now),
            queen("Q07", "BUCKFAST",  "Бакфаст-2","LAYING", "ACTIVE", 7,  now - 15 * day, now),
            queen("Q08", "CARNICA",   "Бета",     "LAYING", "ACTIVE", 8,  now - 12 * day, now),
            queen("Q09", "ITALIANA",  "Кордован", "LAYING", "ACTIVE", 21, now - 10 * day, now, isElite = true),
            queen("Q10", "MELLIFERA", null,        "LAYING", "ACTIVE", 22, now -  9 * day, now),
            queen("Q11", "CARNICA",   "Альфа",    "LAYING", "ACTIVE", 41, now -  8 * day, now, isReserved = true),
            queen("Q12", "BUCKFAST",  "Бакфаст-3","LAYING", "ACTIVE", 42, now -  7 * day, now),

            // --- VIRGIN (Q13–Q22) ---
            queen("Q13", "CARNICA",   "Гамма",    "VIRGIN", "ACTIVE", 9,  now - 14 * day, now),
            queen("Q14", "BUCKFAST",  "Бакфаст-4","VIRGIN", "ACTIVE", 10, now - 12 * day, now),
            queen("Q15", "MELLIFERA", null,        "VIRGIN", "ACTIVE", 11, now - 10 * day, now, aggressionScore = 5),
            queen("Q16", "CARNICA",   "Дельта",   "VIRGIN", "ACTIVE", 12, now -  9 * day, now),
            queen("Q17", "CARPATHICA",null,        "VIRGIN", "ACTIVE", 13, now -  8 * day, now),
            queen("Q18", "CARNICA",   "Скленар",  "VIRGIN", "ACTIVE", 23, now - 16 * day, now),
            queen("Q19", "ITALIANA",  "Кордован", "VIRGIN", "ACTIVE", 24, now -  6 * day, now),
            queen("Q20", "BUCKFAST",  "Бакфаст-5","VIRGIN", "ACTIVE", 43, now -  5 * day, now),
            queen("Q21", "MELLIFERA", null,        "VIRGIN", "ACTIVE", 44, now -  4 * day, now),
            queen("Q22", "CARNICA",   "Эпсилон",  "VIRGIN", "ACTIVE", 45, now -  3 * day, now),

            // --- CELL (Q23–Q30) ---
            queen("Q23", "CARNICA",   "Зета",     "CELL",   "ACTIVE", 14, now -  5 * day, now),
            queen("Q24", "BUCKFAST",  "Бакфаст-6","CELL",   "ACTIVE", 15, now -  4 * day, now),
            queen("Q25", "MELLIFERA", null,        "CELL",   "ACTIVE", 31, now -  3 * day, now),
            queen("Q26", "CARPATHICA",null,        "CELL",   "ACTIVE", 32, now -  2 * day, now),
            queen("Q27", "CARNICA",   "Эта",      "CELL",   "ACTIVE", 33, now -  1 * day, now),
            queen("Q28", "ITALIANA",  "Кордован", "CELL",   "ACTIVE", 34, now,             now),
            queen("Q29", "BUCKFAST",  "Бакфаст-7","CELL",   "ACTIVE", 35, now,             now),
            queen("Q30", "MELLIFERA", null,        "CELL",   "ACTIVE", 46, now,             now),

            // --- SOLD (Q31–Q33) ---
            queen("Q31", "CARNICA",   "Тета",     "LAYING", "SOLD",   null, now - 20 * day, now - 10 * day),
            queen("Q32", "BUCKFAST",  "Бакфаст-8","LAYING", "SOLD",   null, now - 18 * day, now -  8 * day),
            queen("Q33", "MELLIFERA", null,        "VIRGIN", "SOLD",   null, now - 15 * day, now -  5 * day),

            // --- LOST (Q34–Q35) ---
            queen("Q34", "CARNICA",   "Йота",     "VIRGIN", "LOST",   null, now - 25 * day, now - 10 * day),
            queen("Q35", "CARPATHICA",null,        "CELL",   "LOST",   null, now - 22 * day, now -  7 * day)
        )
        db.queenDao().insertAll(queens)

        // Link queens to nucs by updating current_queen_id
        queens.filter { it.lifecycleStatus == "ACTIVE" && it.nucId != null }.forEach { q ->
            db.nucDao().setQueen(q.nucId!!, q.id)
        }
    }

    // -------------------------------------------------------------------------
    // Tasks — 20 auto-generated tasks (overdue MATING_FLIGHT for Q18 + misc)
    // -------------------------------------------------------------------------
    private suspend fun seedTasks(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        val tasks = listOf(
            // Q18/nuc23 — 16 days old VIRGIN → OVERDUE mating flight
            task(nucId = 23, queenId = "Q18", type = "MATING_FLIGHT", dueDate = now - 2 * day),

            // Upcoming MATING_FLIGHT for other virgins
            task(nucId =  9, queenId = "Q13", type = "MATING_FLIGHT", dueDate = now + 1 * day),
            task(nucId = 10, queenId = "Q14", type = "MATING_FLIGHT", dueDate = now + 2 * day),
            task(nucId = 11, queenId = "Q15", type = "MATING_FLIGHT", dueDate = now + 3 * day),
            task(nucId = 12, queenId = "Q16", type = "MATING_FLIGHT", dueDate = now + 4 * day),
            task(nucId = 13, queenId = "Q17", type = "MATING_FLIGHT", dueDate = now + 5 * day),
            task(nucId = 24, queenId = "Q19", type = "MATING_FLIGHT", dueDate = now + 7 * day),
            task(nucId = 43, queenId = "Q20", type = "MATING_FLIGHT", dueDate = now + 8 * day),
            task(nucId = 44, queenId = "Q21", type = "MATING_FLIGHT", dueDate = now + 9 * day),
            task(nucId = 45, queenId = "Q22", type = "MATING_FLIGHT", dueDate = now + 10 * day),

            // HATCHING tasks for cells
            task(nucId = 14, queenId = "Q23", type = "HATCHING", dueDate = now + 2 * day),
            task(nucId = 15, queenId = "Q24", type = "HATCHING", dueDate = now + 3 * day),
            task(nucId = 31, queenId = "Q25", type = "HATCHING", dueDate = now + 4 * day),

            // CHECK_EGGS tasks for laying queens
            task(nucId =  1, queenId = "Q01", type = "CHECK_EGGS", dueDate = now + 1 * day),
            task(nucId =  2, queenId = "Q02", type = "CHECK_EGGS", dueDate = now + 2 * day),

            // FEEDING tasks
            task(nucId = 21, queenId = null, type = "FEEDING", dueDate = now + 1 * day),
            task(nucId = 22, queenId = null, type = "FEEDING", dueDate = now + 2 * day),
            task(nucId = 41, queenId = null, type = "FEEDING", dueDate = now + 3 * day),

            // TREATMENT tasks
            task(nucId = 42, queenId = null, type = "TREATMENT", dueDate = now + 7 * day),
            task(nucId = 43, queenId = null, type = "TREATMENT", dueDate = now + 14 * day)
        )
        db.taskDao().insertAll(tasks)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun queen(
        id: String,
        genetics: String,
        lineName: String?,
        stage: String,
        lifecycleStatus: String,
        nucId: Int?,
        createdAt: Long,
        updatedAt: Long,
        isElite: Boolean = false,
        isReserved: Boolean = false,
        aggressionScore: Int = 0
    ) = QueenEntity(
        id = id,
        genetics = genetics,
        lineName = lineName,
        stage = stage,
        lifecycleStatus = lifecycleStatus,
        nucId = nucId,
        isElite = isElite,
        isReserved = isReserved,
        aggressionScore = aggressionScore,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun task(
        nucId: Int,
        queenId: String?,
        type: String,
        dueDate: Long
    ) = TaskEntity(
        nucId = nucId,
        queenId = queenId,
        taskType = type,
        dueDate = dueDate,
        isDone = false,
        createdAt = System.currentTimeMillis()
    )
}
