package com.raywenderlich.currencyapp.api

import com.raywenderlich.currencyapp.model.CurrenciesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {

    @GET("api/exrates/rates")
    suspend fun getCurrencies(
        @Query("periodicity")
        periodicity: Int = 0,
        @Query("ondate**")
        onDate: String
    ) : Response<CurrenciesResponse>
}