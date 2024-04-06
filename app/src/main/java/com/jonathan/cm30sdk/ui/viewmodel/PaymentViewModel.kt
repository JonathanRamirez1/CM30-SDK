package com.jonathan.cm30sdk.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentIntentData
import com.jonathan.cm30sdk.data.datasource.remote.model.PaymentIntentResponse
import com.jonathan.cm30sdk.data.repository.CardRepository
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: CardRepository) : ViewModel() {

    private val _tokenResult = MutableLiveData<Result<String>>()
    val tokenResult: LiveData<Result<String>> = _tokenResult

    private val _paymentMethodResult = MutableLiveData<Result<String>>()
    val paymentMethodResult: LiveData<Result<String>> = _paymentMethodResult

    private val _paymentIntentResult = MutableLiveData<Result<PaymentIntentResponse>>()
    val paymentIntentResult: LiveData<Result<PaymentIntentResponse>> = _paymentIntentResult

    /*fun createTokenAndProceed(cardInfo: CardInfo) {
        viewModelScope.launch {
            val tokenResult = repository.createToken(cardInfo)
            tokenResult.onSuccess { tokenJsonObject ->
                val token = tokenJsonObject.get("token").asString
                _tokenResult.postValue(Result.success(token))
                createPaymentMethodAndProceed(token)
            }.onFailure { error ->
                _tokenResult.postValue(Result.failure(error))
            }
        }
    }

    private fun createPaymentMethodAndProceed(token: String) {
        viewModelScope.launch {
            val paymentMethodResult = repository.createPaymentMethod(token)
            paymentMethodResult.onSuccess { paymentMethodId ->
                _paymentMethodResult.postValue(Result.success(paymentMethodId))
                createPaymentIntentAndProceed(1000L, "usd")
            }.onFailure { error ->
                _paymentMethodResult.postValue(Result.failure(error))
            }
        }
    }*/

     fun createPaymentIntentAndProceed(amount: Long, currency: String) {
        viewModelScope.launch {
            val paymentIntentData = PaymentIntentData(amount, currency)
            val paymentIntentResult = repository.createPaymentIntent(paymentIntentData)
            paymentIntentResult.onSuccess { paymentIntentResponse ->
                _paymentIntentResult.postValue(Result.success(paymentIntentResponse))
            }.onFailure { error ->
                _paymentIntentResult.postValue(Result.failure(error))
            }
        }
    }
}

