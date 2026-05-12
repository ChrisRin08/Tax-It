package com.christianrincon.taxit.data

import com.christianrincon.taxit.data.db.CachedZipRateEntity
import com.christianrincon.taxit.data.db.TaxCalculationDao
import com.christianrincon.taxit.data.db.TaxCalculationEntity
import com.christianrincon.taxit.data.db.ZipRateCacheDao
import com.christianrincon.taxit.data.network.TaxApiService
import com.christianrincon.taxit.data.network.ZipLookupApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxRepository @Inject constructor(
    private val apiService: TaxApiService,
    private val zipLookupApiService: ZipLookupApiService,
    private val calculationDao: TaxCalculationDao,
    private val zipRateCacheDao: ZipRateCacheDao
) {
    companion object {
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L
        private const val DEFAULT_RATE = "0"
    }

    suspend fun getTaxRate(zip: String): CachedZipRateEntity {
        val cached = zipRateCacheDao.getCachedRate(zip)
        val now = System.currentTimeMillis()

        if (cached != null && (now - cached.cachedAt) < CACHE_DURATION_MS && cached.isZiptaxCache()) {
            return cached
        }

        return try {
            val response = apiService.getSalesTax(zip)
            val rate = response.results?.firstOrNull()
                ?: throw Exception("No tax rate found for ZIP $zip")
            val combinedRate = rate.taxSales.toSafeRateOrNull()
                ?: throw Exception("No combined tax rate found for ZIP $zip")
            val place = runCatching {
                zipLookupApiService.getZipInfo(zip).places.firstOrNull()
            }.getOrNull()
            val entry = CachedZipRateEntity(
                zipCode = rate.geoPostalCode ?: zip,
                stateRate = rate.stateSalesTax.toSafeRate(),
                countyRate = rate.countySalesTax.toSafeRate(),
                cityRate = rate.citySalesTax.toSafeRate(),
                combinedRate = combinedRate,
                cityName = rate.geoCity ?: place?.placeName.orEmpty(),
                stateName = rate.geoState ?: place?.stateAbbreviation?.ifBlank { place.state }.orEmpty()
            )
            zipRateCacheDao.insertOrReplace(entry)
            entry
        } catch (e: Exception) {
            // Fall back to expired cache rather than surfacing a network error
            cached ?: throw e
        }
    }

    suspend fun saveCalculation(
        zip: String,
        cityName: String,
        stateName: String,
        combinedRate: String,
        subtotal: Double,
        tax: Double,
        total: Double
    ) {
        calculationDao.insert(
            TaxCalculationEntity(
                zipCode = zip,
                cityName = cityName,
                stateName = stateName,
                combinedRate = combinedRate,
                subtotal = subtotal,
                taxAmount = tax,
                total = total
            )
        )
    }

    fun getAllCalculations(): Flow<List<TaxCalculationEntity>> =
        calculationDao.getAllCalculations()

    suspend fun deleteCalculation(id: Long) {
        calculationDao.deleteCalculation(id)
    }

    suspend fun deleteAllCalculations() {
        calculationDao.deleteAllCalculations()
    }

    private fun Double?.toSafeRate(): String = toSafeRateOrNull() ?: DEFAULT_RATE

    private fun Double?.toSafeRateOrNull(): String? = this?.takeIf { it >= 0.0 }?.toString()

    private fun CachedZipRateEntity.isZiptaxCache(): Boolean =
        cityRate.toDoubleOrNull() != null &&
            countyRate.toDoubleOrNull() != null &&
            combinedRate.toDoubleOrNull() != null &&
            combinedRate.toDouble() > stateRate.toDoubleOrNull().orZero() &&
            cityName.isNotBlank() &&
            stateName.isNotBlank()

    private fun Double?.orZero(): Double = this ?: 0.0
}
