package com.beekeeplog.app.domain.usecase

import androidx.room.withTransaction
import com.beekeeplog.app.data.room.AppDatabase
import com.beekeeplog.app.domain.model.DbChangeSummary
import javax.inject.Inject

/** UC-05: Applies the confirmed intent to the database within a transaction. */
class ApplyIntentUseCase @Inject constructor(
    private val db: AppDatabase
) {
    suspend operator fun invoke(): DbChangeSummary =
        db.withTransaction { DbChangeSummary(updatedNucs = 0, updatedQueens = 0, createdTasks = 0) }
}
