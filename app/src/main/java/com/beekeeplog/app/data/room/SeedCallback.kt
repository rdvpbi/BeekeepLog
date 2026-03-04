package com.beekeeplog.app.data.room

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject
import javax.inject.Provider

/**
 * Room database creation callback.
 * No seed data — the database starts empty so beekeeper records real hives.
 */
class SeedCallback @Inject constructor(
    @Suppress("unused")
    private val databaseProvider: Provider<AppDatabase>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Intentionally empty: no test data pre-loaded.
    }
}
