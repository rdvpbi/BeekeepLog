package com.beekeeplog.app.data.repo

import com.beekeeplog.app.data.room.dao.QueenDao
import com.beekeeplog.app.data.room.entity.QueenEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** [QueenRepository] implementation backed by [QueenDao]. */
class QueenRepositoryImpl @Inject constructor(
    private val queenDao: QueenDao
) : QueenRepository {

    override fun getAllFlow(): Flow<List<QueenEntity>> = queenDao.getAllFlow()
    override suspend fun getById(id: String): QueenEntity? = queenDao.getById(id)
    override suspend fun getByNucId(nucId: Int): QueenEntity? = queenDao.getByNucId(nucId)
    override suspend fun insert(queen: QueenEntity) = queenDao.insert(queen)
    override suspend fun update(queen: QueenEntity) = queenDao.update(queen)
    override suspend fun updateStage(queenId: String, stage: String, updatedAt: Long) =
        queenDao.updateStage(queenId, stage, updatedAt)
    override suspend fun updateLifecycleStatus(queenId: String, status: String, updatedAt: Long) =
        queenDao.updateLifecycleStatus(queenId, status, updatedAt)
}
