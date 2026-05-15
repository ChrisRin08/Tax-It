package com.christianrincon.taxit.data.network

import com.christianrincon.taxit.data.model.SalesTaxRate
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface for the Ziptax sales tax endpoint.
interface TaxApiService {
    // Looks up tax details by ZIP code using the postalcode query parameter.
    @GET("request/v60")
    suspend fun getSalesTax(@Query("postalcode") zip: String): SalesTaxRate
}
