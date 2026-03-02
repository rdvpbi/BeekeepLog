package com.beekeeplog.app.data.repo

import com.beekeeplog.app.data.room.entity.QueenEntity
import kotlinx.coroutines.flow.Flow

/** Repository interface for queen bee data. */
interface QueenRepository {
    fun getAllFlow(): Flow<List<QueenEntity>>
    suspend fun getById(id: String): QueenEntity?
    suspend fun getByNucId(nucId: Int): QueenEntity?
    suspend fun insert(queen: QueenEntity)
    suspend fun update(queen: QueenEntity)
    suspend fun updateStage(queenId: String, stage: String, updatedAt: Long)
    suspend fun updateLifecycleStatus(queenId: String, status: String, updatedAt: Long)
}
