package com.jonathan.cm30sdk.data.repository

import com.google.gson.JsonObject
import com.jonathan.cm30sdk.data.datasource.remote.model.CardInfo
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentIntentData
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentIntentResponse
import com.jonathan.cm30sdk.data.datasource.remote.model.TokenRequest
import com.jonathan.cm30sdk.data.datasource.remote.network.ApiCM30

class CardRepositoryImpl(private val apiCM30: ApiCM30): CardRepository {

    override suspend fun createToken(cardInfo: CardInfo): Result<JsonObject> {
        return try {
            val response = apiCM30.createToken(cardInfo)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(RuntimeException("Failed to create token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPaymentMethod(token: String): Result<String> {
        return try {
            val response = apiCM30.createPaymentMethod(TokenRequest(token))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()?.paymentMethodId ?: "")
            } else {
                Result.failure(RuntimeException("Failed to create payment method"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPaymentIntent(paymentIntentData: PaymentIntentData): Result<PaymentIntentResponse> {
        return try {
            val response = apiCM30.createPaymentIntent(paymentIntentData)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(RuntimeException("Failed to create payment intent"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
