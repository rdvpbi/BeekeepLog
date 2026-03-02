package com.beekeeplog.app.di

import com.beekeeplog.app.data.repo.NucRepository
import com.beekeeplog.app.data.repo.NucRepositoryImpl
import com.beekeeplog.app.data.repo.QueenRepository
import com.beekeeplog.app.data.repo.QueenRepositoryImpl
import com.beekeeplog.app.data.repo.SessionRepository
import com.beekeeplog.app.data.repo.SessionRepositoryImpl
import com.beekeeplog.app.data.repo.TaskRepository
import com.beekeeplog.app.data.repo.TaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt module that binds repository interfaces to their implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindNucRepository(impl: NucRepositoryImpl): NucRepository
    @Binds @Singleton abstract fun bindQueenRepository(impl: QueenRepositoryImpl): QueenRepository
    @Binds @Singleton abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
    @Binds @Singleton abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}
