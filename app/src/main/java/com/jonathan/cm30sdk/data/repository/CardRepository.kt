package com.jonathan.cm30sdk.data.repository

import com.google.gson.JsonObject
import com.jonathan.cm30sdk.data.datasource.remote.model.CardInfo
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentIntentData
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentIntentResponse

interface CardRepository {
    suspend fun createToken(cardInfo: CardInfo): Result<JsonObject>
    suspend fun createPaymentMethod(token: String): Result<String>
    suspend fun createPaymentIntent(paymentIntentData: PaymentIntentData): Result<PaymentIntentResponse>
}