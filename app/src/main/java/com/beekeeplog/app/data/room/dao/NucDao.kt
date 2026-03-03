package com.beekeeplog.app.data.room.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.beekeeplog.app.data.room.entity.NucEntity
import kotlinx.coroutines.flow.Flow

/** POJO for NUC + QUEEN join results used by analytics queries. */
data class NucWithQueenTuple(
    @ColumnInfo(name = "nucId")    val nucId: Int,
    @ColumnInfo(name = "sector")   val sector: String,
    @ColumnInfo(name = "nucRow")   val nucRow: String?,
    @ColumnInfo(name = "position") val position: String?,
    @ColumnInfo(name = "currentQueenId") val currentQueenId: String?,
    @ColumnInfo(name = "nucNotes") val nucNotes: String?,
    @ColumnInfo(name = "queenId")  val queenId: String?,
    @ColumnInfo(name = "genetics") val genetics: String?,
    @ColumnInfo(name = "lineName") val lineName: String?,
    @ColumnInfo(name = "stage")    val stage: String?,
    @ColumnInfo(name = "lifecycleStatus") val lifecycleStatus: String?,
    @ColumnInfo(name = "isElite")  val isElite: Boolean,
    @ColumnInfo(name = "isReserved") val isReserved: Boolean,
    @ColumnInfo(name = "aggressionScore") val aggressionScore: Int?,
    @ColumnInfo(name = "qualityNotes") val qualityNotes: String?,
    @ColumnInfo(name = "colorMark") val colorMark: String?,
    @ColumnInfo(name = "queenCreatedAt") val queenCreatedAt: Long?,
    @ColumnInfo(name = "stageChangedAt") val stageChangedAt: Long?,
    @ColumnInfo(name = "taskDescription") val taskDescription: String?,
    @ColumnInfo(name = "taskDueAt") val taskDueAt: Long?
)

/** DAO for the `nucs` table. */
@Dao
interface NucDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nuc: NucEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nucs: List<NucEntity>)

    @Update
    suspend fun update(nuc: NucEntity)

    @Query("SELECT * FROM nucs ORDER BY id ASC")
    fun getAllFlow(): Flow<List<NucEntity>>

    @Query("SELECT * FROM nucs WHERE id = :id")
    suspend fun getById(id: Int): NucEntity?

    @Query("UPDATE nucs SET current_queen_id = :queenId WHERE id = :nucId")
    suspend fun setQueen(nucId: Int, queenId: String?)

    @Query("""
        SELECT
            n.id            AS nucId,
            n.sector        AS sector,
            n.row           AS nucRow,
            n.position      AS position,
            n.current_queen_id AS currentQueenId,
            n.notes         AS nucNotes,
            q.id            AS queenId,
            q.genetics      AS genetics,
            q.line_name     AS lineName,
            q.stage         AS stage,
            q.lifecycle_status AS lifecycleStatus,
            COALESCE(q.is_elite, 0) AS isElite,
            COALESCE(q.is_reserved, 0) AS isReserved,
            q.aggression_score AS aggressionScore,
            q.quality_notes AS qualityNotes,
            q.color_mark    AS colorMark,
            q.created_at    AS queenCreatedAt,
            q.stage_changed_at AS stageChangedAt,
            t.description   AS taskDescription,
            t.due_at        AS taskDueAt
        FROM nucs n
        LEFT JOIN queens q ON n.current_queen_id = q.id
        LEFT JOIN tasks t ON t.nuc_id = n.id AND t.is_completed = 0
            AND t.due_at = (
                SELECT MIN(t2.due_at) FROM tasks t2
                WHERE t2.nuc_id = n.id AND t2.is_completed = 0
            )
        ORDER BY n.id ASC
    """)
    fun getAllWithQueenFlow(): Flow<List<NucWithQueenTuple>>

    @Query("""
        SELECT
            n.id AS nucId, n.sector AS sector, n.row AS nucRow, n.position AS position,
            n.current_queen_id AS currentQueenId, n.notes AS nucNotes,
            q.id AS queenId, q.genetics AS genetics, q.line_name AS lineName,
            q.stage AS stage, q.lifecycle_status AS lifecycleStatus,
            COALESCE(q.is_elite, 0) AS isElite, COALESCE(q.is_reserved, 0) AS isReserved,
            q.aggression_score AS aggressionScore, q.quality_notes AS qualityNotes,
            q.color_mark AS colorMark, q.created_at AS queenCreatedAt,
            q.stage_changed_at AS stageChangedAt,
            t.description AS taskDescription, t.due_at AS taskDueAt
        FROM nucs n
        LEFT JOIN queens q ON n.current_queen_id = q.id
        LEFT JOIN tasks t ON t.nuc_id = n.id AND t.is_completed = 0
            AND t.due_at = (SELECT MIN(t2.due_at) FROM tasks t2 WHERE t2.nuc_id = n.id AND t2.is_completed = 0)
        WHERE q.lifecycle_status = 'ACTIVE'
            AND q.stage = 'LAYING'
            AND (q.is_reserved IS NULL OR q.is_reserved = 0)
        ORDER BY n.id ASC
    """)
    fun getForSaleFlow(): Flow<List<NucWithQueenTuple>>

    @Query("""
        SELECT
            n.id AS nucId, n.sector AS sector, n.row AS nucRow, n.position AS position,
            n.current_queen_id AS currentQueenId, n.notes AS nucNotes,
            q.id AS queenId, q.genetics AS genetics, q.line_name AS lineName,
            q.stage AS stage, q.lifecycle_status AS lifecycleStatus,
            COALESCE(q.is_elite, 0) AS isElite, COALESCE(q.is_reserved, 0) AS isReserved,
            q.aggression_score AS aggressionScore, q.quality_notes AS qualityNotes,
            q.color_mark AS colorMark, q.created_at AS queenCreatedAt,
            q.stage_changed_at AS stageChangedAt,
            t.description AS taskDescription, t.due_at AS taskDueAt
        FROM nucs n
        LEFT JOIN queens q ON n.current_queen_id = q.id
        LEFT JOIN tasks t ON t.nuc_id = n.id AND t.is_completed = 0
            AND t.due_at = (SELECT MIN(t2.due_at) FROM tasks t2 WHERE t2.nuc_id = n.id AND t2.is_completed = 0)
        WHERE t.due_at < :now AND t.is_completed = 0
        ORDER BY t.due_at ASC
    """)
    fun getOverdueFlow(now: Long): Flow<List<NucWithQueenTuple>>

    @Query("""
        SELECT
            n.id AS nucId, n.sector AS sector, n.row AS nucRow, n.position AS position,
            n.current_queen_id AS currentQueenId, n.notes AS nucNotes,
            q.id AS queenId, q.genetics AS genetics, q.line_name AS lineName,
            q.stage AS stage, q.lifecycle_status AS lifecycleStatus,
            COALESCE(q.is_elite, 0) AS isElite, COALESCE(q.is_reserved, 0) AS isReserved,
            q.aggression_score AS aggressionScore, q.quality_notes AS qualityNotes,
            q.color_mark AS colorMark, q.created_at AS queenCreatedAt,
            q.stage_changed_at AS stageChangedAt,
            t.description AS taskDescription, t.due_at AS taskDueAt
        FROM nucs n
        LEFT JOIN queens q ON n.current_queen_id = q.id
        LEFT JOIN tasks t ON t.nuc_id = n.id AND t.is_completed = 0
            AND t.due_at = (SELECT MIN(t2.due_at) FROM tasks t2 WHERE t2.nuc_id = n.id AND t2.is_completed = 0)
        WHERE n.current_queen_id IS NULL
            OR EXISTS (SELECT 1 FROM tasks t3 WHERE t3.nuc_id = n.id AND t3.is_completed = 0 AND t3.due_at < :now)
        ORDER BY n.id ASC
    """)
    fun getAvralFlow(now: Long): Flow<List<NucWithQueenTuple>>

    @Query("""
        SELECT
            n.id AS nucId, n.sector AS sector, n.row AS nucRow, n.position AS position,
            n.current_queen_id AS currentQueenId, n.notes AS nucNotes,
            q.id AS queenId, q.genetics AS genetics, q.line_name AS lineName,
            q.stage AS stage, q.lifecycle_status AS lifecycleStatus,
            COALESCE(q.is_elite, 0) AS isElite, COALESCE(q.is_reserved, 0) AS isReserved,
            q.aggression_score AS aggressionScore, q.quality_notes AS qualityNotes,
            q.color_mark AS colorMark, q.created_at AS queenCreatedAt,
            q.stage_changed_at AS stageChangedAt,
            t.description AS taskDescription, t.due_at AS taskDueAt
        FROM nucs n
        LEFT JOIN queens q ON n.current_queen_id = q.id AND q.lifecycle_status = 'ACTIVE'
        LEFT JOIN tasks t ON t.nuc_id = n.id AND t.is_completed = 0
            AND t.due_at = (SELECT MIN(t2.due_at) FROM tasks t2 WHERE t2.nuc_id = n.id AND t2.is_completed = 0)
        WHERE (:genetics IS NULL OR q.genetics = :genetics)
            AND (:lineName IS NULL OR q.line_name = :lineName)
            AND (:stage IS NULL OR q.stage = :stage)
            AND (:sector IS NULL OR n.sector = :sector)
            AND (:nucRow IS NULL OR n.row = :nucRow)
        ORDER BY n.id ASC
    """)
    fun getFilteredFlow(
        genetics: String?,
        lineName: String?,
        stage: String?,
        sector: String?,
        nucRow: String?
    ): Flow<List<NucWithQueenTuple>>
}
