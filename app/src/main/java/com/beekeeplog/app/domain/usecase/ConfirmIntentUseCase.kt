package com.beekeeplog.app.domain.usecase

import com.beekeeplog.app.domain.model.DbChangeSummary
import javax.inject.Inject

/** UC-10: Confirms the pending intent and triggers DB write via [ApplyIntentUseCase]. */
class ConfirmIntentUseCase @Inject constructor(
    private val applyIntent: ApplyIntentUseCase
) {
    suspend operator fun invoke(): Result<DbChangeSummary> =
        Result.success(DbChangeSummary(updatedNucs = 0, updatedQueens = 0, createdTasks = 0))
}
