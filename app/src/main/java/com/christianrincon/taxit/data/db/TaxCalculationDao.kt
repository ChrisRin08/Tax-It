package com.christianrincon.taxit.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxCalculationDao {

    // Inserts one completed calculation into history.
    @Insert
    suspend fun insert(calculation: TaxCalculationEntity)

    // Deletes a single history item by its database id.
    @Query("DELETE FROM tax_calculations WHERE id = :id")
    suspend fun deleteCalculation(id: Long)

    // Deletes every saved history item.
    @Query("DELETE FROM tax_calculations")
    suspend fun deleteAllCalculations()

    // Watches history so the UI updates whenever Room data changes.
    @Query("SELECT * FROM tax_calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<TaxCalculationEntity>>
}
