package com.jonathan.cm30sdk.data.datasource.remote.model

data class CardInfo(
    val cardNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvc: String,
    val amount: Double,
    val currency: String
)
