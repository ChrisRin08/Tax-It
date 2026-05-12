package com.christianrincon.taxit.model

data class HistoryEntry(
    val id: Long = 0,
    val zip: String = "",
    val cityState: String = "",
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val timestamp: Long = 0L
)
