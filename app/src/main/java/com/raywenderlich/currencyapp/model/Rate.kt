package com.raywenderlich.currencyapp.model

data class Rate(
    val rate: Double,
    var rateTomorrow: Double,
    val iso: String,
    val code: Int,
    val quantity: Int,
    val date: String,
    val name: String,
    var isChecked: Boolean = false
)