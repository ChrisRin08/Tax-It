package com.christianrincon.taxit.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tax_calculations")
data class TaxCalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zipCode: String,
    val cityName: String,
    val stateName: String,
    val combinedRate: String,
    val subtotal: Double,
    val taxAmount: Double,
    val total: Double,
    val timestamp: Long = System.currentTimeMillis()
)
