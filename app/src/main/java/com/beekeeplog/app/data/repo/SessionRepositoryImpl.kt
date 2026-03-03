package com.beekeeplog.app.data.repo

import com.beekeeplog.app.data.room.dao.EventDao
import com.beekeeplog.app.data.room.dao.SegmentDao
import com.beekeeplog.app.data.room.dao.SessionDao
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.data.room.entity.InspectionSegmentEntity
import com.beekeeplog.app.data.room.entity.InspectionSessionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** [SessionRepository] implementation backed by Session, Segment and Event DAOs. */
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val segmentDao: SegmentDao,
    private val eventDao: EventDao
) : SessionRepository {

    override suspend fun insertSession(session: InspectionSessionEntity) = sessionDao.insert(session)
    override suspend fun updateSession(session: InspectionSessionEntity) = sessionDao.update(session)
    override suspend fun getSessionById(id: String): InspectionSessionEntity? = sessionDao.getById(id)
    override suspend fun getActiveSession(): InspectionSessionEntity? = sessionDao.getActiveSession()

    override suspend fun insertSegment(segment: InspectionSegmentEntity) = segmentDao.insert(segment)
    override suspend fun updateSegment(segment: InspectionSegmentEntity) = segmentDao.update(segment)
    override fun getSegmentsBySession(sessionId: String): Flow<List<InspectionSegmentEntity>> =
        segmentDao.getBySessionFlow(sessionId)
    override suspend fun getLastPendingSegment(sessionId: String): InspectionSegmentEntity? =
        segmentDao.getLastPendingForSession(sessionId)

    override suspend fun insertEvent(event: EventEntity) = eventDao.insert(event)
    override fun getEventsBySession(sessionId: String): Flow<List<EventEntity>> =
        eventDao.getBySessionFlow(sessionId)
}
