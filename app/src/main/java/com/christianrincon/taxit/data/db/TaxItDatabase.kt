package com.christianrincon.taxit.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

// Defines the Room database tables used by TaxIt.
@Database(
    entities = [TaxCalculationEntity::class, CachedZipRateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaxItDatabase : RoomDatabase() {
    // DAO for saved calculations shown on the History screen.
    abstract fun taxCalculationDao(): TaxCalculationDao

    // DAO for cached ZIP tax rates used by the Calculator screen.
    abstract fun zipRateCacheDao(): ZipRateCacheDao
}
