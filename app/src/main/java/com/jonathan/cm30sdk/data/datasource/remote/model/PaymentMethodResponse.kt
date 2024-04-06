package com.jonathan.cm30sdk.data.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class PaymentMethodResponse(
    @SerializedName("paymentMethodId")
    val paymentMethodId: String
)
