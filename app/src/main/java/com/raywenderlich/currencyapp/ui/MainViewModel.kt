package com.raywenderlich.currencyapp.ui

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.api.RetrofitInstance
import com.raywenderlich.currencyapp.model.NationalRateListResponse
import com.raywenderlich.currencyapp.model.Rate
import com.raywenderlich.currencyapp.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val connectivityManager: ConnectivityManager,
    private val sp: SharedPreferences,
    val spEditor: SharedPreferences.Editor// Need application to check internet state
) : ViewModel() {

    val currencies = MutableLiveData<Resource<List<Rate>>>()
    var toastShowTime = 0L

    private var todayResponseBody = NationalRateListResponse(listOf())
    private var tomorrowResponseBody = NationalRateListResponse(listOf())

    var isTomorrowEmpty = false // Field for right appearance of day's name "Завтра\Вчера".
    var dateToday = MutableLiveData<String>()
    var dateTomorrow = MutableLiveData<String>()
    var wordYesterdayOrTomorrow = MutableLiveData<String>()

    // Getting complex objects from sp with help of json
    val initialCodesDefaultJson = Gson().toJson(mutableListOf<Int>())
    val initialCodesJson = sp.getString("InitialCodes", initialCodesDefaultJson)
    val initialCodes: MutableList<Int> =
        Gson().fromJson(initialCodesJson, object : TypeToken<MutableList<Int>>() {}.type)

    val ratesOrderedSettingsScreenDefaultJson = Gson().toJson(mutableListOf<Rate>())
    var ratesOrderedSettingsScreenJson =
        sp.getString("RatesOrderedSettingsScreen", ratesOrderedSettingsScreenDefaultJson)
    var ratesOrderedSettingsScreen: MutableList<Rate> = Gson().fromJson(
        ratesOrderedSettingsScreenJson,
        object : TypeToken<MutableList<Rate>>() {}.type
    )

    private val initiallyVisibleCurrencies = listOf<Int>(Codes.EUR, Codes.RUB, Codes.USD)

    init {
        getCurrencies()
    }

    fun getCurrencies() = viewModelScope.launch {
        safeCurrenciesCall()
    }

    private suspend fun safeCurrenciesCall() {
        currencies.postValue(Resource.Loading())
        isTomorrowEmpty = false
        try {
            if (hasInternetConnection()) {
                if (!currenciesCall()) return
                if (!tomorrowCurrenciesCall()) return
                // Make yesterday request only if tomorrow's is empty
                if (tomorrowResponseBody.rates.isEmpty()) {
                    if (!yesterdayCurrenciesCall()) return
                }
                // We have to reorder rates to correspond initial order saved in sp
                val todaysResponseBodyOrdered = orderTodaysResponseBody()
                addTomorrowRatesToItems(todaysResponseBodyOrdered)
                // Create settings list only if it doesn't exist
                if (ratesOrderedSettingsScreen.isEmpty()) writeSettingsList(todaysResponseBodyOrdered)

                currencies.postValue(Resource.Success(todaysResponseBodyOrdered))
                dateToday.postValue(getDateTime(Day.TODAY).toString("dd.MM"))
                var dayYesterdayOrTomorrow = if (isTomorrowEmpty) Day.YESTERDAY else Day.TOMORROW
                dateTomorrow.postValue(getDateTime(dayYesterdayOrTomorrow).toString("dd.MM"))
                wordYesterdayOrTomorrow.postValue(if (isTomorrowEmpty) "Вчера" else "Завтра")

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

    private fun hasInternetConnection(): Boolean {
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

    private suspend fun currenciesCall(): Boolean {
        val todayResponse = RetrofitInstance.api.getCurrencies()
        when {
            todayResponse.isSuccessful -> {
                todayResponseBody = todayResponse.body() ?: todayResponseBody
                writeInitialCodes(todayResponseBody.rates)
                return true
            }
            !todayResponse.isSuccessful -> {
                currencies.postValue(Resource.Error(todayResponse.message()))
                return false
            }
        }
        return true
    }

    private suspend fun tomorrowCurrenciesCall(): Boolean {
        val tomorrowResponse =
            RetrofitInstance.api.getCurrencies(date = getDateTime(Day.TOMORROW).toString("dd.MM.yyyy"))
        when {
            tomorrowResponse.isSuccessful -> {
                tomorrowResponseBody =
                    tomorrowResponse.body()!!
                return true
            }

            !tomorrowResponse.isSuccessful -> {
                currencies.postValue(Resource.Error(tomorrowResponse.message()))
                return false
            }
        }
        return true
    }

    private suspend fun yesterdayCurrenciesCall(): Boolean {
        isTomorrowEmpty = true
        val yesterdayResponse =
            RetrofitInstance.api.getCurrencies(
                date = getDateTime(Day.YESTERDAY).toString(
                    "dd.MM.yyyy"
                )
            )
        when {
            yesterdayResponse.isSuccessful -> {
                tomorrowResponseBody =
                    yesterdayResponse.body()!!
                return true
            }

            !yesterdayResponse.isSuccessful -> {
                currencies.postValue(Resource.Error(yesterdayResponse.message()))
                return false
            }
        }
        return true
    }

    private fun orderTodaysResponseBody(): MutableList<Rate>{
        val todaysResponseBodyOrdered: MutableList<Rate> = mutableListOf()
        // Populate ordered list with response items
        for (code in initialCodes) {
            for (rate in todayResponseBody.rates) {
                if (rate.code == code) {
                    todaysResponseBodyOrdered.add(rate)
                    break
                }
            }
        }
        return todaysResponseBodyOrdered
    }

    private fun addTomorrowRatesToItems(todaysResponseBodyOrdered: MutableList<Rate>) {
        for (_rate in todaysResponseBodyOrdered) {
            for (rate in tomorrowResponseBody.rates) {
                if (_rate.code == rate.code) {
                    todaysResponseBodyOrdered[todaysResponseBodyOrdered.indexOf(_rate)].rateTomorrow =
                        rate.rate
                    break
                }
            }
        }
    }

    private fun writeInitialCodes(responseRates: List<Rate>) {
        // When app is launched first time, we capture order of rates by their codes
        // and save to SharedPreferences. To place rates in future according
        // the initial order
        if (initialCodes.isEmpty()) {
            responseRates.forEach { initialCodes.add(it.code) }
            val initialCodesJson = Gson().toJson(initialCodes)
            spEditor.putString("InitialCodes", initialCodesJson)
        }
    }

    private fun writeSettingsList (todaysResponseBodyOrdered: MutableList<Rate>) {
        ratesOrderedSettingsScreen = todaysResponseBodyOrdered
        ratesOrderedSettingsScreen.changeState(initiallyVisibleCurrencies, true)
        val ratesOrderedSettingsScreenJson = Gson().toJson(ratesOrderedSettingsScreen)
        spEditor.putString("RatesOrderedSettingsScreen", ratesOrderedSettingsScreenJson)
    }
}
