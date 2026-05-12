package com.christianrincon.taxit.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_zip_rates")
data class CachedZipRateEntity(
    @PrimaryKey val zipCode: String,
    val stateRate: String,
    val countyRate: String,
    val cityRate: String,
    val combinedRate: String,
    val cityName: String,
    val stateName: String,
    val cachedAt: Long = System.currentTimeMillis()
)
