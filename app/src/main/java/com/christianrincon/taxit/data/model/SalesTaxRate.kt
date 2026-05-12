package com.christianrincon.taxit.data.model

data class SalesTaxRate(
    val version: String?,
    val rCode: Int?,
    val results: List<SalesTaxResult>?
)

data class SalesTaxResult(
    val geoPostalCode: String?,
    val geoCity: String?,
    val geoCounty: String?,
    val geoState: String?,
    val taxSales: Double?,
    val stateSalesTax: Double?,
    val countySalesTax: Double?,
    val citySalesTax: Double?,
    val districtSalesTax: Double?
)
