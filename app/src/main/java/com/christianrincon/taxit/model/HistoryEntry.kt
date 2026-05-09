package com.christianrincon.taxit.model

data class HistoryEntry(
    val zip: String = "",
    val cityState: String = "",
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0
)