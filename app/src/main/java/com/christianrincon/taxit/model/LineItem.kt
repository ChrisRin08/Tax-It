package com.christianrincon.taxit.model

data class LineItem(
    var description: String = "",
    var quantity: Int = 1,
    var price: Double = 0.0
) {
    // Returns the total for this single line item
    fun lineTotal(): Double = quantity * price
}