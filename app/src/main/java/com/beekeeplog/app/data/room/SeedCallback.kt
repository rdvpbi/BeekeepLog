package com.beekeeplog.app.data.room

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.beekeeplog.app.data.room.entity.NucEntity
import com.beekeeplog.app.data.room.entity.QueenEntity
import com.beekeeplog.app.data.room.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider

/**
 * Pre-populates the database with 50 nucs, 35 queens and tasks on first creation.
 * Injected via Hilt and registered with [RoomDatabase.Builder.addCallback].
 * Breeding lines per spec 6.2–6.4: Тройзек 1075, Пешец, Скленар, Б-24, Анатолика, Элгон,
 * Вучковская, Кордован, Альфа.
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
    // Nucs — 50 nuclei across sectors A/B/C/D
    // Empty nucs: 16-20, 25-30, 36-40, 47-50
    // -------------------------------------------------------------------------
    private suspend fun seedNucs(db: AppDatabase) {
        val nucs = buildList {
            for (i in 1..10)  add(NucEntity(id = i,  sector = "A", row = "1", position = i.toString(),        currentQueenId = null))
            for (i in 11..20) add(NucEntity(id = i,  sector = "A", row = "2", position = (i - 10).toString(), currentQueenId = null))
            for (i in 21..30) add(NucEntity(id = i,  sector = "B", row = "1", position = (i - 20).toString(), currentQueenId = null))
            for (i in 31..40) add(NucEntity(id = i,  sector = "B", row = "2", position = (i - 30).toString(), currentQueenId = null))
            for (i in 41..50) add(NucEntity(id = i,  sector = "C", row = "1", position = (i - 40).toString(), currentQueenId = null))
        }
        db.nucDao().insertAll(nucs)
    }

    // -------------------------------------------------------------------------
    // Queens — breeding lines per spec 6.2-6.4
    // -------------------------------------------------------------------------
    private suspend fun seedQueens(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        val queens = listOf(
            // LAYING Q01–Q12
            queen("Q01", "CARNICA",    "Альфа",        "LAYING", "ACTIVE", 1,  now - 30*day, now),
            queen("Q02", "BUCKFAST",   "Тройзек 1075", "LAYING", "ACTIVE", 2,  now - 28*day, now),
            queen("Q03", "MELLIFERA",  null,            "LAYING", "ACTIVE", 3,  now - 25*day, now),
            queen("Q04", "CARNICA",    "Скленар",       "LAYING", "ACTIVE", 4,  now - 22*day, now),
            queen("Q05", "CARPATHICA", null,            "LAYING", "ACTIVE", 5,  now - 20*day, now),
            queen("Q06", "ITALIANA",   "Кордован",      "LAYING", "ACTIVE", 6,  now - 18*day, now),
            queen("Q07", "BUCKFAST",   "Пешец",         "LAYING", "ACTIVE", 7,  now - 15*day, now),
            queen("Q08", "CARNICA",    "Вучковская",    "LAYING", "ACTIVE", 8,  now - 12*day, now),
            queen("Q09", "ITALIANA",   "Кордован",      "LAYING", "ACTIVE", 21, now - 10*day, now, isElite = true),
            queen("Q10", "MELLIFERA",  null,            "LAYING", "ACTIVE", 22, now -  9*day, now),
            queen("Q11", "CARNICA",    "Альфа",         "LAYING", "ACTIVE", 41, now -  8*day, now, isReserved = true),
            queen("Q12", "BUCKFAST",   "Б-24",          "LAYING", "ACTIVE", 42, now -  7*day, now),

            // VIRGIN Q13–Q22
            queen("Q13", "CARNICA",    "Элгон",         "VIRGIN", "ACTIVE", 9,  now - 14*day, now),
            queen("Q14", "BUCKFAST",   "Тройзек 1075",  "VIRGIN", "ACTIVE", 10, now - 12*day, now),
            queen("Q15", "MELLIFERA",  null,            "VIRGIN", "ACTIVE", 11, now - 10*day, now, aggressionScore = 5),
            queen("Q16", "CARNICA",    "Анатолика",     "VIRGIN", "ACTIVE", 12, now -  9*day, now),
            queen("Q17", "CARPATHICA", null,            "VIRGIN", "ACTIVE", 13, now -  8*day, now),
            queen("Q18", "CARNICA",    "Скленар",       "VIRGIN", "ACTIVE", 23, now - 16*day, now),
            queen("Q19", "ITALIANA",   "Кордован",      "VIRGIN", "ACTIVE", 24, now -  6*day, now),
            queen("Q20", "BUCKFAST",   "Пешец",         "VIRGIN", "ACTIVE", 43, now -  5*day, now),
            queen("Q21", "MELLIFERA",  null,            "VIRGIN", "ACTIVE", 44, now -  4*day, now),
            queen("Q22", "CARNICA",    "Вучковская",    "VIRGIN", "ACTIVE", 45, now -  3*day, now),

            // CELL Q23–Q30
            queen("Q23", "CARNICA",    "Элгон",         "CELL",   "ACTIVE", 14, now -  5*day, now),
            queen("Q24", "BUCKFAST",   "Б-24",          "CELL",   "ACTIVE", 15, now -  4*day, now),
            queen("Q25", "MELLIFERA",  null,            "CELL",   "ACTIVE", 31, now -  3*day, now),
            queen("Q26", "CARPATHICA", null,            "CELL",   "ACTIVE", 32, now -  2*day, now),
            queen("Q27", "CARNICA",    "Анатолика",     "CELL",   "ACTIVE", 33, now -  1*day, now),
            queen("Q28", "ITALIANA",   "Кордован",      "CELL",   "ACTIVE", 34, now,          now),
            queen("Q29", "BUCKFAST",   "Тройзек 1075",  "CELL",   "ACTIVE", 35, now,          now),
            queen("Q30", "MELLIFERA",  null,            "CELL",   "ACTIVE", 46, now,          now),

            // SOLD Q31–Q33
            queen("Q31", "CARNICA",    "Скленар",       "LAYING", "SOLD",   null, now - 20*day, now - 10*day),
            queen("Q32", "BUCKFAST",   "Тройзек 1075",  "LAYING", "SOLD",   null, now - 18*day, now -  8*day),
            queen("Q33", "MELLIFERA",  null,            "VIRGIN", "SOLD",   null, now - 15*day, now -  5*day),

            // LOST Q34–Q35
            queen("Q34", "CARNICA",    "Альфа",         "VIRGIN", "LOST",   null, now - 25*day, now - 10*day),
            queen("Q35", "CARPATHICA", null,            "CELL",   "LOST",   null, now - 22*day, now -  7*day)
        )
        db.queenDao().insertAll(queens)

        // Link queens to nucs
        queens.filter { it.lifecycleStatus == "ACTIVE" && it.nucId != null }.forEach { q ->
            db.nucDao().setQueen(q.nucId!!, q.id)
        }
    }

    // -------------------------------------------------------------------------
    // Tasks — auto-generated per spec
    // -------------------------------------------------------------------------
    private suspend fun seedTasks(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        val tasks = listOf(
            // Q18/nuc23 — 16 days old VIRGIN → OVERDUE mating flight
            task(nucId = 23, queenId = "Q18", type = "MATING_FLIGHT", dueAt = now - 2*day, desc = "Облёт Скленар Q18"),

            // Upcoming MATING_FLIGHT for virgins
            task(nucId =  9, queenId = "Q13", type = "MATING_FLIGHT", dueAt = now + 1*day,  desc = "Облёт Элгон Q13"),
            task(nucId = 10, queenId = "Q14", type = "MATING_FLIGHT", dueAt = now + 2*day,  desc = "Облёт Тройзек Q14"),
            task(nucId = 11, queenId = "Q15", type = "MATING_FLIGHT", dueAt = now + 3*day,  desc = "Облёт Q15"),
            task(nucId = 12, queenId = "Q16", type = "MATING_FLIGHT", dueAt = now + 4*day,  desc = "Облёт Анатолика Q16"),
            task(nucId = 13, queenId = "Q17", type = "MATING_FLIGHT", dueAt = now + 5*day,  desc = "Облёт Q17"),
            task(nucId = 24, queenId = "Q19", type = "MATING_FLIGHT", dueAt = now + 7*day,  desc = "Облёт Кордован Q19"),
            task(nucId = 43, queenId = "Q20", type = "MATING_FLIGHT", dueAt = now + 8*day,  desc = "Облёт Пешец Q20"),
            task(nucId = 44, queenId = "Q21", type = "MATING_FLIGHT", dueAt = now + 9*day,  desc = "Облёт Q21"),
            task(nucId = 45, queenId = "Q22", type = "MATING_FLIGHT", dueAt = now + 10*day, desc = "Облёт Вучковская Q22"),

            // HATCHING tasks for cells
            task(nucId = 14, queenId = "Q23", type = "HATCHING", dueAt = now + 2*day, desc = "Выход Элгон Q23"),
            task(nucId = 15, queenId = "Q24", type = "HATCHING", dueAt = now + 3*day, desc = "Выход Б-24 Q24"),
            task(nucId = 31, queenId = "Q25", type = "HATCHING", dueAt = now + 4*day, desc = "Выход Q25"),

            // CHECK_EGGS for laying queens
            task(nucId =  1, queenId = "Q01", type = "CHECK_EGGS", dueAt = now + 1*day, desc = "Проверка яиц Альфа Q01"),
            task(nucId =  2, queenId = "Q02", type = "CHECK_EGGS", dueAt = now + 2*day, desc = "Проверка яиц Тройзек Q02"),

            // FEEDING
            task(nucId = 21, queenId = null, type = "FEEDING", dueAt = now + 1*day, desc = "Кормление нуклеус 21"),
            task(nucId = 22, queenId = null, type = "FEEDING", dueAt = now + 2*day, desc = "Кормление нуклеус 22"),
            task(nucId = 41, queenId = null, type = "FEEDING", dueAt = now + 3*day, desc = "Кормление нуклеус 41"),

            // TREATMENT
            task(nucId = 42, queenId = null, type = "TREATMENT", dueAt = now + 7*day,  desc = "Обработка нуклеус 42"),
            task(nucId = 43, queenId = null, type = "TREATMENT", dueAt = now + 14*day, desc = "Обработка нуклеус 43")
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
        stageChangedAt = createdAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun task(
        nucId: Int,
        queenId: String?,
        type: String,
        dueAt: Long,
        desc: String? = null
    ) = TaskEntity(
        id = UUID.randomUUID().toString(),
        nucId = nucId,
        queenId = queenId,
        taskType = type,
        dueAt = dueAt,
        isCompleted = false,
        description = desc,
        createdAt = System.currentTimeMillis()
    )
}
