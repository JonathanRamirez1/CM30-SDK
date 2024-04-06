package com.jonathan.cm30sdk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jonathan.cm30sdk.data.repository.CardRepository
import com.jonathan.cm30sdk.data.repository.RepositoryFactory

class PaymentViewModelFactory : ViewModelProvider.Factory {

    private val cardRepository: CardRepository = RepositoryFactory.createCardRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(cardRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
