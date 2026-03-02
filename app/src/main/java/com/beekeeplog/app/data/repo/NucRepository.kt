package com.beekeeplog.app.data.repo

import com.beekeeplog.app.data.room.entity.NucEntity
import kotlinx.coroutines.flow.Flow

/** Repository interface for nucleus hive data. */
interface NucRepository {
    fun getAllFlow(): Flow<List<NucEntity>>
    suspend fun getById(id: Int): NucEntity?
    suspend fun setQueen(nucId: Int, queenId: String?)
}
