package com.jonathan.cm30sdk.data.repository

import com.jonathan.cm30sdk.di.NetworkModule

object RepositoryFactory {

    fun createCardRepository(): CardRepository = CardRepositoryImpl(NetworkModule.provideApiService())
}
