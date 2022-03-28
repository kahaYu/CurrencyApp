package com.raywenderlich.currencyapp.api

import com.raywenderlich.currencyapp.model.NationalRateListResponse
import com.raywenderlich.currencyapp.utils.Day
import com.raywenderlich.currencyapp.utils.getDateTime
import com.raywenderlich.currencyapp.utils.toString
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {

    @GET("public/nationalRates")
    suspend fun getCurrencies(
        @Query("currencyCode")
        currencyCode: Array<Int> = arrayOf(),
        @Query("date")
        date: String = getDateTime(Day.TODAY).toString("dd.MM.yyyy")
    ) : Response<NationalRateListResponse>
}