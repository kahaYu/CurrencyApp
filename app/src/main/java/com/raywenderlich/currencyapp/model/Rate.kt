package com.raywenderlich.currencyapp.model

data class Rate(
    val rate: Double,
    val iso: String,
    val code: Int,
    val quantity: Int,
    val date: String,
    val name: String
)