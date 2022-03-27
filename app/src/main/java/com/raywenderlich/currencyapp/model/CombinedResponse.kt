package com.raywenderlich.currencyapp.model

data class CombinedResponse(
    val todayResponse: List<Rate>,
    val tomorrowResponse: List<Double>?
)