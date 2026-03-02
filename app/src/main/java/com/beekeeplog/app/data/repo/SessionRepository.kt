package com.beekeeplog.app.data.repo

import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.data.room.entity.InspectionSegmentEntity
import com.beekeeplog.app.data.room.entity.InspectionSessionEntity
import kotlinx.coroutines.flow.Flow

/** Repository interface for inspection sessions, segments and events. */
interface SessionRepository {
    suspend fun insertSession(session: InspectionSessionEntity)
    suspend fun updateSession(session: InspectionSessionEntity)
    suspend fun getSessionById(id: String): InspectionSessionEntity?
    suspend fun getActiveSession(): InspectionSessionEntity?

    suspend fun insertSegment(segment: InspectionSegmentEntity): Long
    suspend fun updateSegment(segment: InspectionSegmentEntity)
    fun getSegmentsBySession(sessionId: String): Flow<List<InspectionSegmentEntity>>

    suspend fun insertEvent(event: EventEntity): Long
    fun getEventsBySession(sessionId: String): Flow<List<EventEntity>>
}
