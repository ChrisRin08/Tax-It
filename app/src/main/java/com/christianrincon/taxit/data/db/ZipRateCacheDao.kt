package com.christianrincon.taxit.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ZipRateCacheDao {

    // Saves or replaces the cached rate for a ZIP code.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(cachedRate: CachedZipRateEntity)

    // Finds a cached tax rate before making a network call.
    @Query("SELECT * FROM cached_zip_rates WHERE zipCode = :zip LIMIT 1")
    suspend fun getCachedRate(zip: String): CachedZipRateEntity?

    // Removes old cache rows when cleanup is needed.
    @Query("DELETE FROM cached_zip_rates WHERE cachedAt < :expiryTime")
    suspend fun deleteExpiredRates(expiryTime: Long)
}
