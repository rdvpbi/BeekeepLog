package com.beekeeplog.app.data.repo

import com.beekeeplog.app.data.room.dao.NucDao
import com.beekeeplog.app.data.room.entity.NucEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** [NucRepository] implementation backed by [NucDao]. */
class NucRepositoryImpl @Inject constructor(
    private val nucDao: NucDao
) : NucRepository {

    override fun getAllFlow(): Flow<List<NucEntity>> = nucDao.getAllFlow()

    override suspend fun getById(id: Int): NucEntity? = nucDao.getById(id)

    override suspend fun setQueen(nucId: Int, queenId: String?) = nucDao.setQueen(nucId, queenId)
}
