package com.raywenderlich.currencyapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.currencyapp.model.CurrenciesResponse
import com.raywenderlich.currencyapp.di.CurrencyApplication
import com.raywenderlich.currencyapp.api.RetrofitInstance
import com.raywenderlich.currencyapp.utils.Resource
import com.raywenderlich.currencyapp.utils.getCurrentDateTime
import com.raywenderlich.currencyapp.utils.toString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val connectivityManager: ConnectivityManager // Need application to check internet state
) : ViewModel() {

    val currencies = MutableLiveData<Resource<CurrenciesResponse>>()
    var toastShowTime = 0L

    init {

        getCurrencies()
    }

    fun getCurrencies() = viewModelScope.launch {
        safeCurrenciesCall()
    }

    private suspend fun safeCurrenciesCall() {
        currencies.postValue(Resource.Loading())
        val currentDate = getCurrentDateTime().toString("yyyy-MM-dd")
        try {
            if (hasInternetConnection()) {
                val response = RetrofitInstance.api.getCurrencies(0, currentDate)
                when {
                    response.isSuccessful ->
                        currencies.postValue(Resource.Success(response.body()!!))
                    !response.isSuccessful ->
                        currencies.postValue(Resource.Error(response.message()))
                }
            } else {
                currencies.postValue(Resource.Error("нет интернет соединения"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> currencies.postValue(Resource.Error("сеть недоступна"))
                else -> currencies.postValue(Resource.Error("конвертер JSON"))
            }
        }
    }

    fun hasInternetConnection(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}
