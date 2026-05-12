package com.christianrincon.taxit.data.network

import com.christianrincon.taxit.data.model.SalesTaxRate
import retrofit2.http.GET
import retrofit2.http.Query

interface TaxApiService {
    @GET("request/v60")
    suspend fun getSalesTax(@Query("postalcode") zip: String): SalesTaxRate
}
