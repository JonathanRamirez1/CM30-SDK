package com.jonathan.cm30sdk.data.datasource.remote.network

import com.google.gson.JsonObject
import com.jonathan.cm30sdk.data.datasource.remote.model.CardInfo
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentIntentData
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentIntentResponse
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentMethodResponse
import com.jonathan.cm30sdk.data.datasource.remote.model.TokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiCM30 {

    @POST("api/payments/create-token")
    suspend fun createToken(@Body cardInfo: CardInfo): Response<JsonObject>

    @POST("api/payments/create-payment-method")
    suspend fun createPaymentMethod(@Body token: TokenRequest): Response<PaymentMethodResponse>

    @POST("api/payments/create-payment-intent")
    suspend fun createPaymentIntent(@Body paymentIntentData: PaymentIntentData): Response<PaymentIntentResponse>
}
