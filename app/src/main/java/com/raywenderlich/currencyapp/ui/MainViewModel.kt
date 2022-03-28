package com.raywenderlich.currencyapp.ui

import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.currencyapp.api.RetrofitInstance
import com.raywenderlich.currencyapp.model.CombinedResponse
import com.raywenderlich.currencyapp.model.NationalRateListResponse
import com.raywenderlich.currencyapp.model.Rate
import com.raywenderlich.currencyapp.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val connectivityManager: ConnectivityManager // Need application to check internet state
) : ViewModel() {

    val currencies = MutableLiveData<Resource<List<Rate>>>()
    var toastShowTime = 0L

    var todayResponseBody: NationalRateListResponse? = null
    var tomorrowResponseBody: NationalRateListResponse? = null

    var isTomorrowEmpty = false

    init {
        getCurrencies()
    }

    fun getCurrencies() = viewModelScope.launch {
        safeCurrenciesCall()
    }

    private suspend fun safeCurrenciesCall() {
        currencies.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val todayResponse = RetrofitInstance.api.getCurrencies()
                when {
                    todayResponse.isSuccessful -> todayResponseBody = todayResponse.body()!!
                    !todayResponse.isSuccessful -> {
                        currencies.postValue(Resource.Error(todayResponse.message()))
                        return
                    }
                }
                val tomorrowResponse =
                    RetrofitInstance.api.getCurrencies(date = getDateTime(Day.TOMORROW).toString("dd.MM.yyyy"))
                when {
                    tomorrowResponse.isSuccessful -> tomorrowResponseBody =
                        tomorrowResponse.body()!!

                    !tomorrowResponse.isSuccessful -> {
                        currencies.postValue(Resource.Error(tomorrowResponse.message()))
                        return
                    }
                }
                // Make yesterday request only if tomorrow's is empty
                if (tomorrowResponseBody?.rates!!.isEmpty()) {
                    isTomorrowEmpty = true
                    val yesterdayResponse =
                        RetrofitInstance.api.getCurrencies(
                            date = getDateTime(Day.YESTERDAY).toString(
                                "dd.MM.yyyy"
                            )
                        )
                    when {
                        yesterdayResponse.isSuccessful -> tomorrowResponseBody =
                            yesterdayResponse.body()!!

                        !yesterdayResponse.isSuccessful -> {
                            currencies.postValue(Resource.Error(yesterdayResponse.message()))
                            return
                        }
                    }
                }
                // Depending on date API returns different order of rates
                // We have to reorder tomorrow's rates to correspond today's rates
                val tomorrowResponseBodyOrdered: MutableList<Double> = mutableListOf()

                for (currency in todayResponseBody!!.rates) {
                    for (rate in tomorrowResponseBody!!.rates) {
                        if (rate.code == currency.code) {
                            todayResponseBody!!.rates[todayResponseBody!!.rates.indexOf(currency)].rateTomorrow =
                                rate.rate
                            break
                        }
                    }
                }

                val combinedResponse = todayResponseBody!!.rates

                currencies.postValue(Resource.Success(combinedResponse))

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
