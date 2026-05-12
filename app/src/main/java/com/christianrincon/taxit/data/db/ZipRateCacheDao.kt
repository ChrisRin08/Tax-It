package com.christianrincon.taxit.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ZipRateCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(cachedRate: CachedZipRateEntity)

    @Query("SELECT * FROM cached_zip_rates WHERE zipCode = :zip LIMIT 1")
    suspend fun getCachedRate(zip: String): CachedZipRateEntity?

    @Query("DELETE FROM cached_zip_rates WHERE cachedAt < :expiryTime")
    suspend fun deleteExpiredRates(expiryTime: Long)
}
