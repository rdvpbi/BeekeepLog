package com.beekeeplog.app.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.beekeeplog.app.data.room.converter.Converters
import com.beekeeplog.app.data.room.dao.EventDao
import com.beekeeplog.app.data.room.dao.NucDao
import com.beekeeplog.app.data.room.dao.QueenDao
import com.beekeeplog.app.data.room.dao.SegmentDao
import com.beekeeplog.app.data.room.dao.SessionDao
import com.beekeeplog.app.data.room.dao.TaskDao
import com.beekeeplog.app.data.room.entity.EventEntity
import com.beekeeplog.app.data.room.entity.InspectionSegmentEntity
import com.beekeeplog.app.data.room.entity.InspectionSessionEntity
import com.beekeeplog.app.data.room.entity.NucEntity
import com.beekeeplog.app.data.room.entity.QueenEntity
import com.beekeeplog.app.data.room.entity.TaskEntity

/** Single Room database for the entire app. */
@Database(
    entities = [
        NucEntity::class,
        QueenEntity::class,
        TaskEntity::class,
        InspectionSessionEntity::class,
        InspectionSegmentEntity::class,
        EventEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nucDao(): NucDao
    abstract fun queenDao(): QueenDao
    abstract fun taskDao(): TaskDao
    abstract fun sessionDao(): SessionDao
    abstract fun segmentDao(): SegmentDao
    abstract fun eventDao(): EventDao
}
