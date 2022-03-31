package com.raywenderlich.currencyapp.ui

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    var todayResponseBody: NationalRateListResponse? = null
    var tomorrowResponseBody: NationalRateListResponse? = null

    var isTomorrowEmpty = false
    var dateToday = MutableLiveData<String>()
    var dateTomorrow = MutableLiveData<String>()
    var wordYesterdayOrTomorrow = MutableLiveData<String>()

    // Getting complex objects from sp with help of json
    val initialCodesDefaultJson = Gson().toJson(mutableListOf<Int>())
    val initialCodesJson = sp.getString("InitialCodes", initialCodesDefaultJson)
    val initialCodes: MutableList<Int> =
        Gson().fromJson(initialCodesJson, object : TypeToken<MutableList<Int>>() {}.type)

    val ratesOrderedSettingsScreenDefaultJson = Gson().toJson(mutableListOf<Rate>())
    var ratesOrderedSettingsScreenJson = sp.getString("RatesOrderedSettingsScreen", ratesOrderedSettingsScreenDefaultJson)
    var ratesOrderedSettingsScreen: MutableList<Rate> = Gson().fromJson(ratesOrderedSettingsScreenJson,
        object : TypeToken<MutableList<Rate>>() {}.type
    )

    init {
        //ratesOrderedSettingsScreenJson =
        //    sp.getString("RatesOrderedSettingsScreen", ratesOrderedSettingsScreenDefaultJson)!!
        //ratesOrderedSettingsScreen = Gson().fromJson(
        //    ratesOrderedSettingsScreenJson,
        //    object : TypeToken<MutableList<Rate>>() {}.type
        //)
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
                val todayResponse = RetrofitInstance.api.getCurrencies()
                when {
                    todayResponse.isSuccessful -> {
                        todayResponseBody = todayResponse.body()!!
                        // When app is launched first time, we capture order of rates by their codes
                        // and save to SharedPreferences. To place rates in future according
                        // the initial order
                        if (initialCodes.isEmpty()) {
                            todayResponseBody?.rates?.forEach { initialCodes.add(it.code) }
                            val initialCodesJson = Gson().toJson(initialCodes)
                            spEditor.putString("InitialCodes", initialCodesJson)
                        }
                    }
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
                // We have to reorder rates to correspond initial order saved in sp
                val todaysResponseBodyOrdered: MutableList<Rate> = mutableListOf()
                // Populate ordered list with response items
                for (code in initialCodes) {
                    for (rate in todayResponseBody!!.rates) {
                        if (code == rate.code) {
                            todaysResponseBodyOrdered.add(rate)
                            break
                        }
                    }
                }
                // Add tomorrow rates to the items
                for (_rate in todaysResponseBodyOrdered) {
                    for (rate in tomorrowResponseBody!!.rates) {
                        if (_rate.code == rate.code) {
                            todaysResponseBodyOrdered[todaysResponseBodyOrdered.indexOf(_rate)].rateTomorrow =
                                rate.rate
                            break
                        }
                    }
                }

                if (ratesOrderedSettingsScreen.isEmpty()) {
                    ratesOrderedSettingsScreen = todaysResponseBodyOrdered
                    val ratesOrderedSettingsScreenJson = Gson().toJson(ratesOrderedSettingsScreen)
                    spEditor.putString("RatesOrderedSettingsScreen", ratesOrderedSettingsScreenJson)
                }

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
