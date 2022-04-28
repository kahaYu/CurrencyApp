package com.raywenderlich.currencyapp.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.view.View
import android.view.animation.Animation
import android.widget.LinearLayout
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
import java.io.LineNumberReader
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val connectivityManager: ConnectivityManager,
    private val sp: SharedPreferences,
    val spEditor: SharedPreferences.Editor
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
    private val initialCodesDefaultJson = Gson().toJson(mutableMapOf<Int, Boolean>())
    private val initialCodesJson = sp.getString("InitialCodes", initialCodesDefaultJson)
    private var initialCodes: MutableMap<Int, Boolean> = // Code attached to state
        Gson().fromJson(
            initialCodesJson,
            object : TypeToken<MutableMap<Int, Boolean>>() {}.type
        )

    // Codes ordered properly. Main field, where we manage order of codes
    private val codesListDefaultJson = Gson().toJson(mutableListOf<Int>())
    private val codesListJson = sp.getString("CodesList", codesListDefaultJson)
    private val codesList: MutableList<Int> =
        Gson().fromJson(
            codesListJson,
            object : TypeToken<MutableList<Int>>() {}.type
        )

    var todaysResponseBodyOrdered = mutableListOf<Rate>()

    var initiallyVisibleCurrencies = mutableListOf<Rate>()
    val modifyedRatesState = mutableListOf<Rate>()
    val modifyedRatesOrder = mutableListOf<Rate>()

    val toastMessage = MutableLiveData<String>()

    var isRefreshing = false

    var shouldRemoveShadow: Boolean = false
    var isNavigatedFromSettingsFragment: Boolean = false

    init {
        getCurrencies()
    }

    fun getCurrencies() = viewModelScope.launch {
        safeCurrenciesCall()
    }

    private suspend fun safeCurrenciesCall() {
        if (!isRefreshing) currencies.postValue(Resource.Loading())
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
                todaysResponseBodyOrdered = orderTodaysResponseBody()
                addTomorrowRatesToItems(todaysResponseBodyOrdered)
                todaysResponseBodyOrdered.changeState(initialCodes)
                initiallyVisibleCurrencies.clear()

                for (currency in todaysResponseBodyOrdered) {
                    if (currency.isChecked) initiallyVisibleCurrencies.add(currency)
                }

                currencies.postValue(Resource.Success(initiallyVisibleCurrencies))
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

    @SuppressLint("ObsoleteSdkInt")
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
                writeInitialCodesIfEmpty(todayResponseBody.rates)
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
                date = getDateTime(Day.YESTERDAY).toString("dd.MM.yyyy")
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

    private fun orderTodaysResponseBody(): MutableList<Rate> {
        val todaysResponseBodyOrdered: MutableList<Rate> = mutableListOf()
        // Populate ordered list with response items
        for (code in initialCodes.keys) {
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

    private fun writeInitialCodesIfEmpty(responseRates: List<Rate>) {
        // When app is launched first time, we capture order of rates by their codes
        // and save to SharedPreferences. To place rates in future according
        // the initial order
        if (initialCodes.isEmpty()) {
            responseRates.forEach {
                initialCodes.put(
                    it.code,
                    it.code == Codes.EUR
                            || it.code == Codes.USD
                            || it.code == Codes.RUB
                )
                codesList.add(it.code)
            }
            val initialCodesJson = Gson().toJson(initialCodes)
            spEditor.putString("InitialCodes", initialCodesJson).apply()
            val codesJson = Gson().toJson(codesList)
            spEditor.putString("CodesList", codesJson).apply()
        }
    }

    private fun writeInitialCodes(responseRates: List<Rate>) {
        initialCodes.clear()
        codesList.clear()
        responseRates.forEach {
            initialCodes.put(
                it.code,
                it.isChecked
            )
            codesList.add(it.code)
        }
        val initialCodesJson = Gson().toJson(initialCodes)
        spEditor.putString("InitialCodes", initialCodesJson).apply()
        val codesJson = Gson().toJson(codesList)
        spEditor.putString("CodesList", codesJson).apply()
    }

    fun mergeChangesToVisibleCurrencies() {
        // Change list for displaying in settings fragment
        todaysResponseBodyOrdered.clear()
        modifyedRatesOrder.forEach { todaysResponseBodyOrdered.add(it) }
        todaysResponseBodyOrdered.changeState(modifyedRatesState)

        writeInitialCodes(todaysResponseBodyOrdered)

        for (rate in modifyedRatesState) {
            when {
                rate.isChecked -> {
                    if (!initiallyVisibleCurrencies.getAllCodes().contains(rate.code))
                        initiallyVisibleCurrencies.add(rate)
                }
                !rate.isChecked -> {
                    if (initiallyVisibleCurrencies.getAllCodes().contains(rate.code))
                        initiallyVisibleCurrencies.removeRateAtCode(rate.code)
                }
            }
        }
        toastMessage.postValue("Сохранено")
        // Sort rates according to initial codes order
        currencies.postValue(Resource.Success(sortVisibleCurrencies(initiallyVisibleCurrencies)))
    }

    private fun sortVisibleCurrencies(initiallyVisibleCurrencies: List<Rate>): List<Rate> {
        val initiallyVisibleCurrenciesSorted = mutableListOf<Rate>()

        for (code in codesList) {
            for (currency in initiallyVisibleCurrencies) {
                if (code == currency.code) {
                    initiallyVisibleCurrenciesSorted.add(currency)
                    break
                }
            }
        }
        return initiallyVisibleCurrenciesSorted
    }

    fun startArrowAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "translationY", 30f).apply {
            repeatCount = Animation.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = 1000
            start()
        }
    }

    fun smoothAppearance(view: View, alpha: Float) {
        view.alpha = 0F
        ObjectAnimator.ofFloat(view, "alpha", alpha).apply {
            duration = 1500
            start()
        }
    }
}
