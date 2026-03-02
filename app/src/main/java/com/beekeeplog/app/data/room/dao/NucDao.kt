package com.beekeeplog.app.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.beekeeplog.app.data.room.entity.NucEntity
import kotlinx.coroutines.flow.Flow

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
}
