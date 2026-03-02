package com.beekeeplog.app.di

import android.content.Context
import androidx.room.Room
import com.beekeeplog.app.data.room.AppDatabase
import com.beekeeplog.app.data.room.SeedCallback
import com.beekeeplog.app.data.room.dao.EventDao
import com.beekeeplog.app.data.room.dao.NucDao
import com.beekeeplog.app.data.room.dao.QueenDao
import com.beekeeplog.app.data.room.dao.SegmentDao
import com.beekeeplog.app.data.room.dao.SessionDao
import com.beekeeplog.app.data.room.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt module that provides the Room database and all DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        seedCallback: SeedCallback
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "beekeeplog.db")
            .addCallback(seedCallback)
            .build()

    @Provides fun provideNucDao(db: AppDatabase): NucDao = db.nucDao()
    @Provides fun provideQueenDao(db: AppDatabase): QueenDao = db.queenDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()
    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
    @Provides fun provideSegmentDao(db: AppDatabase): SegmentDao = db.segmentDao()
    @Provides fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()
}
