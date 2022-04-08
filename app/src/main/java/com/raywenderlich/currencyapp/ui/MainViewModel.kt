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

    private var todayResponseBody = NationalRateListResponse(listOf())
    private var tomorrowResponseBody = NationalRateListResponse(listOf())

    var isTomorrowEmpty = false // Field for right appearance of day's name "Завтра\Вчера".
    var dateToday = MutableLiveData<String>()
    var dateTomorrow = MutableLiveData<String>()
    var wordYesterdayOrTomorrow = MutableLiveData<String>()

    // Getting complex objects from sp with help of json
    val initialCodesDefaultJson = Gson().toJson(mutableMapOf<Int,Boolean>())
    val initialCodesJson = sp.getString("InitialCodes", initialCodesDefaultJson)
    val initialCodes: MutableMap<Int, Boolean> =
        Gson().fromJson(
            initialCodesJson,
            object : TypeToken<MutableMap<Int, Boolean>>() {}.type
        )

    var todaysResponseBodyOrdered = mutableListOf<Rate>()

    private val initiallyVisibleCurrenciesCodes = listOf<Int>(Codes.EUR, Codes.RUB, Codes.USD)
    val newVisibleCurrenciesCodes = mutableListOf<Int>()

    var initiallyVisibleCurrencies = mutableListOf<Rate>()
    val modifyedCurrencies = mutableListOf<Rate>()

    val toastMessage = MutableLiveData<String>()

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
                // 4. Создаём новый список. Располагаем в нём элементы согласно списку кодов
                todaysResponseBodyOrdered = orderTodaysResponseBody()
                addTomorrowRatesToItems(todaysResponseBodyOrdered)
                // 5. Меняем стейт списка Х в соответствие со списком включённых кодов O.
                todaysResponseBodyOrdered.changeState(initialCodes)
                // 6. Создаём список Н элементов для отображения.
                // Добавляем в этот список только те элементы от Х, у которых включена видимость.
                for (currency in todaysResponseBodyOrdered) {
                        if (currency.isChecked) initiallyVisibleCurrencies.add(currency)
                    }

                //// Create settings list only if it doesn't exist
                //if (ratesOrderedSettingsScreen.isEmpty()) writeSettingsList(
                //    todaysResponseBodyOrdered
                //)
                //// Create list of visible items on main screen
                //for (currency in ratesOrderedSettingsScreen) {
                //    if (currency.isChecked) initiallyVisibleCurrencies.add(currency)
                //}
                //newVisibleCurrencies = initiallyVisibleCurrencies
                // We pass to adapter only visible items
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
                // 1. Получаем список всех валют
                todayResponseBody = todayResponse.body() ?: todayResponseBody
                // 2. Создаём список кодов всех полученных валют в виде мапы.
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

    private fun writeInitialCodes(responseRates: List<Rate>) {
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
            }
            // 3. Сохраняем список кодов в SP
            val initialCodesJson = Gson().toJson(initialCodes)
            spEditor.putString("InitialCodes", initialCodesJson)
        }
    }

    fun mergeChangesToVisibleCurrencies() {
        todaysResponseBodyOrdered.changeState(modifyedCurrencies)
        for (rate in modifyedCurrencies) {
            when {
                rate.isChecked -> {
                    if (!initiallyVisibleCurrencies.contains(rate))
                        initiallyVisibleCurrencies.add(rate)
                }
                !rate.isChecked -> {
                    if (initiallyVisibleCurrencies.contains(rate))
                        initiallyVisibleCurrencies.remove(rate)
                }
            }
        }
        toastMessage.postValue("Сохранено")
        currencies.postValue(Resource.Success(initiallyVisibleCurrencies))
    }
}
