package com.christianrincon.taxit.data.network

import com.christianrincon.taxit.data.model.ZipLookupResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ZipLookupApiService {
    @GET("us/{zip}")
    suspend fun getZipInfo(@Path("zip") zip: String): ZipLookupResponse
}
