package com.christianrincon.taxit.data.model

import com.google.gson.annotations.SerializedName

data class ZipLookupResponse(
    @SerializedName("post code") val postCode: String,
    val places: List<ZipLookupPlace>
)

data class ZipLookupPlace(
    @SerializedName("place name") val placeName: String,
    val state: String,
    @SerializedName("state abbreviation") val stateAbbreviation: String
)
