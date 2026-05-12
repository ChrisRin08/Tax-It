package com.christianrincon.taxit.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxCalculationDao {

    @Insert
    suspend fun insert(calculation: TaxCalculationEntity)

    @Query("DELETE FROM tax_calculations WHERE id = :id")
    suspend fun deleteCalculation(id: Long)

    @Query("DELETE FROM tax_calculations")
    suspend fun deleteAllCalculations()

    @Query("SELECT * FROM tax_calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<TaxCalculationEntity>>
}
