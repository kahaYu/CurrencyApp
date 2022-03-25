package com.raywenderlich.currencyapp.api

import com.raywenderlich.currencyapp.model.NationalRateListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {

    @GET("public/nationalRates")
    suspend fun getCurrencies(
        @Query("currencyCode")
        currencyCode: Array<Int> = arrayOf(),
        @Query("date")
        date: String
    ) : Response<NationalRateListResponse>
}