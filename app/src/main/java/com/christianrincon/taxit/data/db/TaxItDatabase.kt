package com.christianrincon.taxit.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TaxCalculationEntity::class, CachedZipRateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaxItDatabase : RoomDatabase() {
    abstract fun taxCalculationDao(): TaxCalculationDao
    abstract fun zipRateCacheDao(): ZipRateCacheDao
}
